using MassTransit;
using Sds.Storage.Blob.Core;
using Serilog;
using Serilog.Events;
using Xunit;
using Xunit.Abstractions;

namespace Leanda.Microscopy.Metadata.Tests
{
    [CollectionDefinition("Microscopy Metadata Test Harness")]
    public class leandaTestCollection : ICollectionFixture<MicroscopyMetadataTestHarness>
    {
    }

    public abstract class MicroscopyMetadataTest
    {
        public MicroscopyMetadataTestHarness Harness { get; }

        protected IBus Bus => Harness.BusControl;
        protected IBlobStorage BlobStorage => Harness.BlobStorage;

        public MicroscopyMetadataTest(MicroscopyMetadataTestHarness fixture, ITestOutputHelper output = null)
        {
            Harness = fixture;

            if (output != null)
            {
                Log.Logger = new LoggerConfiguration()
                    .MinimumLevel.Debug()
                    .WriteTo
                    .TestOutput(output, LogEventLevel.Verbose)
                    .CreateLogger()
                    .ForContext<MicroscopyMetadataTest>();
            }
        }
    }
}
