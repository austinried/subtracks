#!/usr/bin/env -S deno --allow-all
import { SubsonicClient } from "./util/subsonic.ts";
import { sleep } from "./util/util.ts";

async function getArtistId(
  client: SubsonicClient,
  artist: string,
): Promise<string> {
  const { xml } = await client.get("getArtists");

  return xml.querySelector(
    `artist[name='${artist.replaceAll("'", "\\'")}']`,
  )?.id!;
}

async function getAlbumId(
  client: SubsonicClient,
  album: string,
): Promise<string> {
  const { xml } = await client.get("getAlbumList2", [
    ["type", "newest"],
  ]);

  return xml.querySelector(
    `album[name='${album.replaceAll("'", "\\'")}']`,
  )?.id!;
}

async function getSongId(
  client: SubsonicClient,
  album: string,
  track: number,
): Promise<string> {
  const albumId = await getAlbumId(client, album);

  const { xml } = await client.get("getAlbum", [["id", albumId!]]);
  return xml.querySelector(`song[track='${track}']`)?.id!;
}

async function scrobbleTrack(
  client: SubsonicClient,
  album: string,
  track: number,
) {
  const songId = await getSongId(client, album, track);

  await client.get("scrobble", [
    ["id", songId!],
    ["submission", "true"],
  ]);
}

async function starAlbum(client: SubsonicClient, album: string) {
  const albumId = await getAlbumId(client, album);

  await client.get("star", [["albumId", albumId]]);
}

async function starArtist(client: SubsonicClient, artist: string) {
  const artistId = await getArtistId(client, artist);

  await client.get("star", [["artistId", artistId]]);
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
  await scrobbleTrack(client, "Retroconnaissance EP", 1);
  await sleep(1_000);
  await scrobbleTrack(client, "Retroconnaissance EP", 2);
  await sleep(1_000);
  await scrobbleTrack(client, "Kosmonaut", 1);

  await createPlaylist(client, "Playlist 1", [
    { album: "Retroconnaissance EP", track: 2 },
    { album: "Retroconnaissance EP", track: 1 },
    { album: "Kosmonaut", track: 2 },
    { album: "Kosmonaut", track: 4 },
    { album: "I Don't Know What I'm Doing", track: 9 },
    { album: "I Don't Know What I'm Doing", track: 10 },
    { album: "I Don't Know What I'm Doing", track: 11 },
  ]);

  await starAlbum(client, "Kosmonaut");
  await starArtist(client, "Ugress");
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
