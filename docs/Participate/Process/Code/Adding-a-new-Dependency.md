---
icon: octicons/package-dependencies-16
---
## Requirements 
You can only add support for plugins that have a public API. This means Maven must be able to resolve the dependency 
from an online repository. Adding dependencies from you local hard-drive is NOT allowed as this stops everyone from 
building the plugin.

## Adding a new repo
Open up the pom.xml file located in the project's folder. Check if the new dependencies' repository already exists in our 
list of repositories. If that's the case, search for the dependency block related to that repository - there are comments
above these blocks indicating that.

If there is no such repository tag, add it.
New repository tags need to be added in this format: `betonquest-<repoName>-repo`.
Then add a new dependency block for that repository. There needs to be a comment above that dependency block that indicates
which repository holds this dependency. Take a look at the other blocks for guidance.

## Finishing up
We speed our builds up using our own mirror repository. It needs to be configured in your local Maven settings file as
shown on the [Setup Project](../../Setup-Project.md#build-speed-up) page.
**Please add any new repositories to your local file and to [that documentation page](../../Setup-Project.md#build-speed-up).**
