Cuando se genera un pull request

GitHub automáticamente ejecuta el job build-and-test para verificar que el código cumple con las reglas de calidad. no ejecuta el job deploy porque no es un push a master.

Si pasan las pruebas el boton del Pull Request se activa y un programador puede hacer el merge.

Cuando hace el merge, el workflow se ejecuta de nuevo, pero esta vez se ejecuta el job build-and-test yel job deploy se ejecuta porque es un push a master.
