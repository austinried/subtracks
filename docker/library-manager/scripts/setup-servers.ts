#!/usr/bin/env -S deno --allow-all
import { SubsonicClient } from "./util/subsonic.ts";
import { sleep } from "./util/util.ts";

async function getSongId(
  client: SubsonicClient,
  album: string,
  track: number,
): Promise<string> {
  const { xml: albumsXml } = await client.get("getAlbumList2", [
    ["type", "newest"],
  ]);
  const albumId = albumsXml.querySelector(
    `album[name='${album.replaceAll("'", "\\'")}']`,
  )?.id;

  const { xml: songsXml } = await client.get("getAlbum", [["id", albumId!]]);
  return songsXml.querySelector(`song[track='${track}']`)?.id!;
}

async function scrobbleTrack(
  client: SubsonicClient,
  songId: string,
) {
  await client.get("scrobble", [
    ["id", songId!],
    ["submission", "true"],
  ]);
}

async function createPlaylist(
  client: SubsonicClient,
  name: string,
  songs: { album: string; track: number }[],
) {
  const songIds = await Promise.all(songs.map(({ album, track }) => {
    return getSongId(client, album, track);
  }));

  await client.get("createPlaylist", [
    ["name", name],
    ...songIds.map((songId) => ["songId", songId] as [string, string]),
  ]);
}

async function setupTestData(client: SubsonicClient) {
  await scrobbleTrack(
    client,
    await getSongId(client, "Retroconnaissance EP", 1),
  );
  await sleep(1_000);
  await scrobbleTrack(
    client,
    await getSongId(client, "Retroconnaissance EP", 2),
  );
  await sleep(1_000);
  await scrobbleTrack(client, await getSongId(client, "Kosmonaut", 1));

  await createPlaylist(client, "Playlist 1", [
    { album: "Retroconnaissance EP", track: 2 },
    { album: "Retroconnaissance EP", track: 1 },
    { album: "Kosmonaut", track: 2 },
    { album: "Kosmonaut", track: 4 },
    { album: "I Don't Know What I'm Doing", track: 9 },
    { album: "I Don't Know What I'm Doing", track: 10 },
    { album: "I Don't Know What I'm Doing", track: 11 },
  ]);
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
