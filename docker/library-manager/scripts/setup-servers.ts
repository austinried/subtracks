#!/usr/bin/env -S deno --allow-all

async function setupNavidrome() {
  console.log("setting up navidrome...");

  await fetch("http://navidrome:4533/auth/createAdmin", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: "admin",
      password: "password",
    }),
  });
}

await setupNavidrome();

console.log("setup-servers complete");
