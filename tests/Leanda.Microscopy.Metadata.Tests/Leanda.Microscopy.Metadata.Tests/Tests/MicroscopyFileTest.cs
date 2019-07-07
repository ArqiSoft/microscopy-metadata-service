using FluentAssertions;
using System;
using Xunit;
using Xunit.Abstractions;

namespace Leanda.Microscopy.Metadata.Tests
{
    public class MicroscopyFileTestFixture
    {
        public Guid UserId { get; } = Guid.NewGuid ();
        public Guid BlobId { get; }
        public string Bucket { get; }
        public Guid Id { get; } = Guid.NewGuid ();
        public Guid CorrelationId { get; } = Guid.NewGuid ();

        public MicroscopyFileTestFixture (MicroscopyMetadataTestHarness harness)
        {
            Bucket = UserId.ToString ();
            BlobId = harness.UploadResource (Bucket, "Nikon_BF007.nd2").Result;
            harness.ExtractMicroscopyMetadata (Id, BlobId, Bucket, UserId, CorrelationId).Wait ();
        }
    }

    [Collection ("Microscopy Metadata Test Harness")]
    public class MicroscopyFileTest : MicroscopyMetadataTest, IClassFixture<MicroscopyFileTestFixture>
    {
        private Guid CorrelationId;
        private string Bucket;
        private Guid UserId;
        private Guid Id;

        public MicroscopyFileTest (MicroscopyMetadataTestHarness harness, ITestOutputHelper output, MicroscopyFileTestFixture initFixture) : base (harness, output)
        {
            Id = initFixture.Id;
            CorrelationId = initFixture.CorrelationId;
            Bucket = initFixture.Bucket;
            UserId = initFixture.UserId;
        }

        [Fact]
        public void MicroscopyFileTest_ValidFile_ShouldCalculateProperties() {
            var evn = Harness.GetEvent(Id);
            evn.UserId.Should().Be(UserId);
            evn.CorrelationId.Should().Be(CorrelationId);
        }
    }
}