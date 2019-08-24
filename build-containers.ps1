docker build -t leanda/microscopy-metadata .
cd tests/Leanda.Microscopy.Metadata.Tests
docker build -t leanda/microscopy-metadata-tests:latest .