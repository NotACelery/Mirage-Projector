# dev.56 — compile repair

Windows `build.bat` reached `:compileJava` and failed on a single source error in `CoreBoosterBlockEntity.empty()`.

The method referenced `material.present()` as if `material` were a local field/variable. The authoritative accessor is `material()`, so the repair is:

```java
return !material().present();
```

No beacon optics, Core Booster behavior, networking, assets, recipes, or protocol values were changed by this repair. Version remains `0.1.0-dev.56`.
