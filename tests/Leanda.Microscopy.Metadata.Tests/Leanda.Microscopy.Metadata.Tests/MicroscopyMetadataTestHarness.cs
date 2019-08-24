using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Leanda.Microscopy.Metadata.Domain.Events;
using MassTransit;
using MassTransit.ExtensionsDependencyInjectionIntegration;
using MassTransit.RabbitMqTransport;
using MassTransit.Scoping;
using MassTransit.Testing.MessageObservers;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using MongoDB.Driver;
using Sds.MassTransit.RabbitMq;
using Sds.Storage.Blob.Core;
using Sds.Storage.Blob.GridFs;
using Serilog;

namespace Leanda.Microscopy.Metadata.Tests
{
    public class MicroscopyMetadataTestHarness : IDisposable
    {
        protected IServiceProvider _serviceProvider;

        public IBlobStorage BlobStorage { get { return _serviceProvider.GetService<IBlobStorage>(); } }

        public IBusControl BusControl { get { return _serviceProvider.GetService<IBusControl>(); } }

        private List<ExceptionInfo> Faults = new List<ExceptionInfo>();
        public ReceivedMessageList Received { get; } = new ReceivedMessageList(TimeSpan.FromSeconds(60));

        public MicroscopyMetadataTestHarness()
        {
            var configuration = new ConfigurationBuilder()
                .AddJsonFile("appsettings.json", true, true)
                .AddEnvironmentVariables()
                .Build();

            Log.Logger = new LoggerConfiguration()
                .CreateLogger();

            Log.Information("Staring Microscopy Metadata tests");

            var services = new ServiceCollection();

            services.AddTransient<IBlobStorage, GridFsStorage>(x =>
            {
                var blobStorageUrl = new MongoUrl(Environment.ExpandEnvironmentVariables(configuration["GridFs:ConnectionString"]));
                var client = new MongoClient(blobStorageUrl);

                return new GridFsStorage(client.GetDatabase(blobStorageUrl.DatabaseName));
            });

            services.AddSingleton<IConsumerScopeProvider, DependencyInjectionConsumerScopeProvider>();

            services.AddSingleton(container => Bus.Factory.CreateUsingRabbitMq(x =>
            {
                var hr = new Uri(Environment.ExpandEnvironmentVariables(configuration["MassTransit:ConnectionString"]));

                IRabbitMqHost host = x.Host(new Uri(Environment.ExpandEnvironmentVariables(configuration["MassTransit:ConnectionString"])), h => { });

                x.RegisterConsumers(host, container, e =>
                {
                    e.UseInMemoryOutbox();
                });

                x.ReceiveEndpoint(host, "processing_fault_queue", e =>
                {
                    e.Handler<Fault>(async context =>
                    {
                        Faults.AddRange(context.Message.Exceptions.Where(ex => !ex.ExceptionType.Equals("System.InvalidOperationException")));

                        await Task.CompletedTask;
                    });
                });

                x.ReceiveEndpoint(host, "processing_update_queue", e =>
                {
                    e.Handler<MicroscopyMetadataExtracted>(context => { Received.Add(context); return Task.CompletedTask; });
                    e.Handler<MicroscopyMetadataExtractionFailed>(context => { Received.Add(context); return Task.CompletedTask; });
                });
            }));

            _serviceProvider = services.BuildServiceProvider();

            var busControl = _serviceProvider.GetRequiredService<IBusControl>();

            busControl.Start();
        }

        public MicroscopyMetadataExtracted GetEvent(Guid id)
        {
            return Received
                .ToList()
                .Where(m => m.Context.GetType().IsGenericType && m.Context.GetType().GetGenericArguments()[0] == typeof(MicroscopyMetadataExtracted))
                .Select(m => (m.Context as ConsumeContext<MicroscopyMetadataExtracted>).Message)
                .Where(m => m.Id == id).ToList().SingleOrDefault();
        }

        public MicroscopyMetadataExtractionFailed GetMicroscopyMetadataExtractionFailedEvent(Guid id)
        {
            return Received
                .ToList()
                .Where(m => m.Context.GetType().IsGenericType && m.Context.GetType().GetGenericArguments()[0] == typeof(MicroscopyMetadataExtractionFailed))
                .Select(m => (m.Context as ConsumeContext<MicroscopyMetadataExtractionFailed>).Message)
                .Where(m => m.Id == id).ToList().SingleOrDefault();
        }

        public virtual void Dispose()
        {
            var busControl = _serviceProvider.GetRequiredService<IBusControl>();
            busControl.Stop();
        }
    }
}