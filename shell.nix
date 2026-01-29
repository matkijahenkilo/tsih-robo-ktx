{
  pkgs ? import <nixpkgs> { },
  pkgMvn ? import <nixpkgs> { },
  devTools ? true,
  ...
}:
pkgs.mkShell {
  shellHook = ''
    echo -e "\u001B[34mNanora!"
  '';

  nativeBuildInputs = [
    pkgs.jdk25
    pkgMvn.maven
  ]
  ++ pkgs.lib.optionals devTools [
    pkgs.gradle # kotlin-language-server dependency
    pkgs.jdt-language-server
    pkgs.kotlin-language-server
  ];
}
