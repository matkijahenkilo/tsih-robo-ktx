{
  lib,
  maven,
  jre,
  makeWrapper,

  ffmpeg,
  yt-dlp,
  gallery-dl,
}:
let
  project-name = "tsih-robo-ktx";
  vers = "0.0.1";
  jar-file = "${project-name}-${vers}-jar-with-dependencies.jar";
in
maven.buildMavenPackage {
  pname = project-name;
  version = vers;

  src = lib.cleanSource ./.;

  mvnHash = "sha256-FSneivfSXdQVlgtzQePParaRtujOFUF0qNxojmS4XNg=";

  mvnParameters = lib.escapeShellArgs [
    "-Dspotless.check.skip"
    "-Dspotless.apply.skip"
  ];

  nativeBuildInputs = [ makeWrapper ];

  buildInputs = [
    ffmpeg
    yt-dlp
    gallery-dl
  ];

  installPhase = ''
    runHook preInstall

    mkdir -p $out/bin $out/share/${project-name}
    install -Dm644 target/${jar-file} $out/share/${project-name}

    makeWrapper ${jre}/bin/java $out/bin/${project-name} \
      --add-flags "-jar $out/share/${project-name}/${jar-file}"

    runHook postInstall
  '';
}