FROM microsoft/dotnet:2.1-sdk AS build-env

ARG RID=linux-x64

WORKDIR /build

COPY Leanda.Microscopy.Metadata.Domain/Leanda.Microscopy.Metadata.Domain.csproj Leanda.Microscopy.Metadata.Domain/
COPY Leanda.Microscopy.Metadata.Tests/Leanda.Microscopy.Metadata.Tests.csproj Leanda.Microscopy.Metadata.Tests/

RUN dotnet restore Leanda.Microscopy.Metadata.Tests/Leanda.Microscopy.Metadata.Tests.csproj

COPY Leanda.Microscopy.Metadata.Domain Leanda.Microscopy.Metadata.Domain
COPY Leanda.Microscopy.Metadata.Tests Leanda.Microscopy.Metadata.Tests

RUN dotnet publish Leanda.Microscopy.Metadata.Tests/Leanda.Microscopy.Metadata.Tests.csproj -r $RID -c Release -o /dist

# Build runtime image
FROM microsoft/dotnet:2.1-sdk

WORKDIR /app

RUN apt-get update && apt-get install -y curl
RUN curl https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh > /app/wait-for-it.sh && chmod 777 /app/wait-for-it.sh

COPY --from=build-env /dist ./

ENTRYPOINT ["dotnet", "vstest", "./Leanda.Microscopy.Metadata.Tests.dll"]
