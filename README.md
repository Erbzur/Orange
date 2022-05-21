# Orange

Orange is a xposed module that support to specify orientation per app, which is useful for some app
that need specified orientation to get better experience (such as MAME4droid).

## Target Platform

| Platform | Min API |
| :------- | :-----: |
| Android  |   28    |  
| Xposed   |   89    |

### Note:

The module config uses content provider, which needs the permissions of `autostart`
and `running in the backgraound`, otherwise it will not work properly.  
After installing the module, set up these permissions via app info setting page.