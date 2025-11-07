// @deno-types="npm:@types/jsdom@27.0.0"
import { JSDOM } from "npm:jsdom@27.1.0";

export class SubsonicClient {
  constructor(
    readonly baseUrl: string,
    readonly username: string,
    readonly password: string,
  ) {}

  async get(
    method: "download",
    params?: [string, string][],
  ): Promise<{ res: Response; xml: undefined }>;
  async get(
    method: string,
    params?: [string, string][],
  ): Promise<{ res: Response; xml: Document }>;
  async get(
    method: string,
    params?: [string, string][],
  ): Promise<{ res: Response; xml: Document | undefined }> {
    const url = new URL(`rest/${method}.view`, this.baseUrl);

    url.searchParams.set("u", this.username);
    url.searchParams.set("p", this.password);
    url.searchParams.set("v", "1.13.0");
    url.searchParams.set("c", "subtracks-test-fixture");

    if (params) {
      for (const [key, value] of params) {
        url.searchParams.append(key, value);
      }
    }

    const res = await fetch(url);

    let xml: Document | undefined;
    if (res.headers.get("content-type")?.includes("xml")) {
      xml = new JSDOM(await res.text(), {
        contentType: "text/xml",
      }).window.document;
    }

    if (!res.ok) {
      let message = `HTTP error ${res.status}`;
      if (xml) {
        const error = xml.querySelector("error");
        const errorCode = error?.getAttribute("code");
        const errorMessage = error?.getAttribute("message");
        message += `\nSubsonic error${errorCode}: ${errorMessage}`;
      }
      throw new Error(message);
    }

    return { res, xml };
  }
}
