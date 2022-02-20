# `:data-bridge` module

Specific module to be used for DI of `:data` module, without exposing its implementation details.

*Fain-in* must be equal to **1** (`:app` module).
*Fan-out*  must be equal to **1** (`:data` module via `implementation`).