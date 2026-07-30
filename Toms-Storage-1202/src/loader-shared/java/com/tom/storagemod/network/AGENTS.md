# Loader Network Features

- Owns the wireless terminal open request; final channel transport remains in `src/main/java/.../network`.
- Never trust client position, item, range, or ownership claims. Resolve the valid terminal server-side and preserve Curios lookup behavior.
- Inspect wireless items, terminal block entities, config range rules, and final `NetworkHandler` before protocol changes.
- Validate normal and advanced wireless opening, out-of-range/cross-dimension rejection, Curios presence/absence, and malformed requests.
