# version_settings() enforces a minimum Tilt version
# https://docs.tilt.dev/api.html#api.version_settings
version_settings(constraint='>=0.22.2')

local_resource(
  'identity-authorization-server-compile',
  '/bin/bash gradlew clean installDist',
  deps=['src', 'build.gradle'])

docker_build(
    'identity-authorization-server',
    context='./build/install',
    dockerfile="./Dockerfile.dev"
)
