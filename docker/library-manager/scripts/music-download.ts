#!/usr/bin/env -S deno --allow-all
import * as path from "jsr:@std/path@1.1.2";
import { MUSIC_DIR } from "./util/env.ts";
import { SubsonicClient } from "./util/subsonic.ts";

const client = new SubsonicClient(
  "http://demo.subsonic.org",
  "guest1",
  "guest",
);

for (const id of ["197", "199", "321"]) {
  const { res } = await client.get("download", [["id", id]]);

  let filename = res.headers.get("Content-Disposition")
    ?.split(";")[1];
  filename = (filename?.includes("*=")
    ? decodeURIComponent(filename.split("''")[1])
    : filename?.split("=")[1]) ?? `${id}.zip`;

  console.log("downloading album:", filename);
  const downloadPath = path.join(MUSIC_DIR, filename);

  const file = await Deno.open(downloadPath, {
    write: true,
    create: true,
  });
  await res.body?.pipeTo(file.writable);

  await new Deno.Command("unzip", {
    args: [
      downloadPath,
      "-d",
      path.join(MUSIC_DIR, filename.split(".")[0]),
    ],
  }).output();

  await Deno.remove(downloadPath);
}

console.log("music-download complete");
