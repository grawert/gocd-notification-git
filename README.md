# GoCD Notification plugin for Gitea

This GoCD plugin is updating Gitea commit status with pipeline state
notifications.

## Building the plugin

To build the plugin jar, run `./gradlew clean test assemble`

## Behavior

On every pipeline run, the commit status is updated according to the pipeline
stage status. The hostname of the material URL has to match the ServerUrl
(see configuration).

## Configuration

You can configure the plugin by navigating to `Admin` -> `Plugins` ->
`Status notification for Gitea` settings.

- **Gitea Server URL**: The URL of your Gitea server (example: https://git.yourcompany.com)
- **API Authentication Token**: Access Token for Gitea API authorization
