{ pkgs ? import (fetchTarball https://nixos.org/channels/nixos-24.11/nixexprs.tar.xz) { } }:
with pkgs;

mkShell {
  buildInputs = [
    maven
    jdk23
    glibc
  ];

  shellHook = ''
    export JAVA_HOME=${jdk23.outPath}
  '';
}
