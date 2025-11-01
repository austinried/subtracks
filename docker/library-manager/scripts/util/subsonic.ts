export class SubsonicClient {
  constructor(
    readonly baseUrl: string,
    readonly username: string,
    readonly password: string,
  ) {}

  get(method: string, params?: Record<string, string>) {
    const url = new URL(`rest/${method}.view`, this.baseUrl);

    url.searchParams.set("u", this.username);
    url.searchParams.set("p", this.password);
    url.searchParams.set("v", "1.13.0");
    url.searchParams.set("c", "subtracks-test-fixture");

    if (params) {
      Object.entries(params).forEach(([key, value]) =>
        url.searchParams.append(key, value)
      );
    }

    return fetch(url);
  }
}
