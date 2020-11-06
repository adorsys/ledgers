# ModelBank custom theme for Keycloak

## What is it?

There is a possibility to deploy the custom theme to Keycloak with ModelBank UI look'n'feel. This will change the appearance
for some pages.

## How to deploy this theme

In order to deploy custom UI, just copy 'modelbank.jar' archive into your Keycloak '/standalone/deployments/' directory. After this,
this theme will be automatically deployed (if your server is running at this moment), and you may select it as an active theme.
To do this, please open admin console, choose the realm you need to customize, then proceed to 'Realm Settings' and select the 'Themes'
tab. The one thing you need to change is 'Login Theme'. Please select 'modelbank' and save the changes.

Keep in mind, that some browsers use caching for UI settings, CSS and pictures, so, to see the changes it may be neccessary
to clean all caches in the browser (or use incognito tab, for example).

## Sources?

You may find the sources of the ModelBank theme in the 'modelbank' directory. The 'modelbank.jar' is ready to use archive with
this theme. It is also possible to create your own theme and deploy it to your Keycloak instance.