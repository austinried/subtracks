#!/usr/bin/env -S deno --allow-all
import { SubsonicClient } from "./util/subsonic.ts";
import { sleep } from "./util/util.ts";

async function scrobbleTrack(
  client: SubsonicClient,
  album: string,
  track: number,
) {
  const { xml: albumsXml } = await client.get("getAlbumList2", {
    type: "newest",
  });
  const albumId = albumsXml.querySelector(`album[name='${album}']`)?.id;

  const { xml: songsXml } = await client.get("getAlbum", { id: albumId! });
  const songId = songsXml.querySelector(`song[track='${track}']`)?.id;

  await client.get("scrobble", {
    id: songId!,
    submission: "true",
  });
}

async function setupTestData(client: SubsonicClient) {
  await scrobbleTrack(client, "Retroconnaissance EP", 1);
  await sleep(1_000);
  await scrobbleTrack(client, "Retroconnaissance EP", 2);
  await sleep(1_000);
  await scrobbleTrack(client, "Kosmonaut", 1);
}

async function setupNavidrome() {
  console.log("setting up navidrome...");

  const baseUrl = "http://navidrome:4533";
  const username = "admin";
  const password = "password";

  await fetch("http://navidrome:4533/auth/createAdmin", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  const client = new SubsonicClient(baseUrl, username, password);
  await setupTestData(client);
}

async function setupGonic() {
  console.log("setting up gonic...");

  const baseUrl = "http://gonic";
  const username = "admin";
  const password = "admin";

  const client = new SubsonicClient(baseUrl, username, password);
  await setupTestData(client);
}

await Promise.all([
  setupNavidrome(),
  setupGonic(),
]);

console.log("setup-servers complete");
