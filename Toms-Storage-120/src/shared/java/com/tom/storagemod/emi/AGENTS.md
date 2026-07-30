# EMI Integration

Own EMI plugin, ghost ingredients, and crafting-terminal transfer for the Forge 1.20.1 line.

- Keep EMI optional and preserve ingredient accounting, menu mapping, and server-authoritative terminal actions; no EMI classes may leak into core startup.
- Inspect shared `gui`/`util`, main networking, `Content`, and the project's EMI compile/runtime dependencies.
- Validate with and without EMI, ghost placement, partial/full inventories, recipe transfer remainders, and dedicated-server loading.
