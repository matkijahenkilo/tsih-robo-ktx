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
    pkgs.jdk
    pkgMvn.maven
  ]
  ++ pkgs.lib.optional devTools [
    pkgs.gradle # kotlin-language-server dependency
    pkgs.jdt-language-server
    pkgs.kotlin-language-server
  ];
}
