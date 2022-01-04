# Worker (JS)

This module contains the browser code compiled to JavaScript and served as the client application's worker.

This code is used from `frontend` via `$package$.frontend.services.WorkerService`.

The root package (`$package$.worker`) contains `JSLauncher` which is used to define routers.

The worker expects a messages which is defined inside `shared` package.

## What's next?

You've probably read all the READMEs in this application. Take a look around the codebase. 
The code itself is documented, so we hope you understand it without an issue.

You can also take a look at the [Udash Developers Guide](http://guide.udash.io/). If you have any questions,
you can ask for help on our [Gitter channel](https://gitter.im/UdashFramework/udash-core).