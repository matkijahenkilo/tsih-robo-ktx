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

  # Maven uses the internet for downloading deps, thus you need to manually
  # update the mvnHash when they change. You can set it to `lib.fakeHash` and
  # get the right one from the error message
  mvnHash = "sha256-Pjm1S+I8IAuAtUtJTR5MGjMyQEERTQT/8MfiGZLgjnI=";

  nativeBuildInputs = [ makeWrapper ];

  installPhase = ''
    runHook preInstall

    mkdir -p $out/bin $out/share/${project-name}
    install -Dm644 target/${jar-file} $out/share/${project-name}

    makeWrapper ${jre}/bin/java $out/bin/${project-name} \
      --add-flags "-jar $out/share/${project-name}/${jar-file}" \
      --prefix PATH : ${
        lib.makeBinPath [
          ffmpeg
          yt-dlp
          gallery-dl
        ]
      }

    runHook postInstall
  '';
}
