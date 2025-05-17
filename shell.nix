{
  pkgs ? import <nixpkgs> { },
  devTools ? true,
  ...
}:
pkgs.mkShell {
  shellHook = ''
    echo -e "\u001B[34mNanora!"
  '';

  nativeBuildInputs =
    with pkgs;
    [
      jdk
      maven
    ]
    ++ pkgs.lib.optional devTools [
      gradle # kotlin-language-server dependency
      jdt-language-server
      kotlin-language-server
    ];
}
