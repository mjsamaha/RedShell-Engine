# Input, assets and events

## Input
Define an `enum` implementing `Action`, bind it to keys in a text block with `BindingLoader.parse`, and ask
`context().input()` for `pressed`, `held` or `released`. Never read raw keys in game code. Bindings can be
remapped at runtime and saved with the settings.

## Assets
Register each file in the `AssetCatalog` with an ID and type, group IDs into an `AssetGroup`, and return the
group from `Scene.assets()`. The engine loads it before the scene enters and unloads it when no scene uses it.
Get an asset with `context().assets().get(ID, Type.class)`. Assets in a group may be reloaded, so do not
cache them past the scene's lifetime.

## Events
`EventBus` is typed publish/subscribe. In a scene call `subscribe(EventType.class, listener)`; it is
unsubscribed automatically when the scene exits.