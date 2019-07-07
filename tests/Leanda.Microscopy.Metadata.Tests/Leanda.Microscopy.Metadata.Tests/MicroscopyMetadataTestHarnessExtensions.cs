using Leanda.Microscopy.Metadata.Domain.Commands;
using MassTransit;
using System;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

namespace Leanda.Microscopy.Metadata.Tests
{
    public static class MicroscopyMetadataTestHarnessExtensions
    {
        public static async Task<Guid> UploadResource(this MicroscopyMetadataTestHarness harness, string bucket, string fileName)
        {
            return await UploadFile(harness, bucket, Path.Combine(Directory.GetCurrentDirectory(), "Resources", fileName));
        }

        public static async Task<Guid> UploadFile(this MicroscopyMetadataTestHarness harness, string bucket, string path)
        {
            var source = new FileStream(path, FileMode.Open, FileAccess.Read);
            return await harness.BlobStorage.AddFileAsync(Path.GetFileName(path), source, "application/octet-stream", bucket);
        }

        public static async Task PublishExtractMetadataCommand(this MicroscopyMetadataTestHarness harness, Guid id, Guid blobId, string bucket, Guid userId, Guid correlationId)
        {
            await harness.BusControl.Publish<ExtractMicroscopyMetadata>(new
            {
                Id = id,
                UserId = userId,
                BlobId = blobId,
                Bucket = bucket,
                CorrelationId = correlationId
            });
        }

        public static async Task ExtractMicroscopyMetadata(this MicroscopyMetadataTestHarness harness, Guid id, Guid blobId, string bucket, Guid userId, Guid correlationId)
        {
            await harness.PublishExtractMetadataCommand(id, blobId, bucket, userId, correlationId);

            harness.WaitWhileProcessingFinished(correlationId);
        }

        public static void WaitWhileProcessingFinished(this MicroscopyMetadataTestHarness harness, Guid correlationId)
        {
            if (!harness.Received.Select<CorrelatedBy<Guid>>(m => m.Context.Message.CorrelationId == correlationId).Any())
            {
                throw new TimeoutException();
            }
        }
    }
}
