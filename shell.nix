{ pkgs ? import (fetchTarball https://nixos.org/channels/nixos-24.11/nixexprs.tar.xz) { } }:
with pkgs;

mkShell {
  buildInputs = [
    maven
    jdk21
    glibc
  ];

  shellHook = ''
    export JAVA_HOME=${jdk21.outPath}
  '';
}
