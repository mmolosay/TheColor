# `:base` module

Base module with shared external dependencies, defined as `api`.
Must never depend on another module and have ***I = 0***, with *Fan-in > 0* and *Fan-out = 0.