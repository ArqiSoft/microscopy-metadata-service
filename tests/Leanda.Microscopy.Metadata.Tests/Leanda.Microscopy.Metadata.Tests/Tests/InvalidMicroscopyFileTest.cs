using FluentAssertions;
using FluentAssertions.Extensions;
using System;
using Xunit;
using Xunit.Abstractions;

namespace Leanda.Microscopy.Metadata.Tests
{
    public class InvalidMicroscopyFileTestFixture
    {
        public Guid UserId { get; } = Guid.NewGuid ();
        public Guid BlobId { get; }
        public string Bucket { get; }
        public Guid Id { get; } = Guid.NewGuid ();
        public Guid CorrelationId { get; } = Guid.NewGuid ();

        public InvalidMicroscopyFileTestFixture (MicroscopyMetadataTestHarness harness)
        {
            Bucket = UserId.ToString ();
            BlobId = Guid.NewGuid();
            harness.CalculateChemicalProperties (Id, BlobId, Bucket, UserId, CorrelationId).Wait ();
        }
    }

    [Collection ("Microscopy Metadata Test Harness")]
    public class InvalidMicroscopyFileTest : MicroscopyMetadataTest, IClassFixture<InvalidMicroscopyFileTestFixture> {
        private Guid CorrelationId;
        private string Bucket;
        private Guid UserId;
        private Guid Id;

        public InvalidMicroscopyFileTest(MicroscopyMetadataTestHarness harness, ITestOutputHelper output, InvalidMicroscopyFileTestFixture initFixture) : base(harness, output)
        {
            Id = initFixture.Id;
            CorrelationId = initFixture.CorrelationId;
            Bucket = initFixture.Bucket;
            UserId = initFixture.UserId;
        }

        [Fact]
        public void MicroscopyMetadataExtracting_ValidFile_ShouldFail()
        { 
           var evn = Harness.GetChemicalPropertiesCalculationFailedEvent(Id);
           evn.Should().NotBeNull();
           evn.UserId.Should().Be(UserId);
           evn.CorrelationId.Should().Be(CorrelationId);
           evn.TimeStamp.Should().BeBefore(DateTime.Now).And.BeLessThan(2000.Milliseconds());
           evn.Message.Should().NotBeNull();
        }
    }
}