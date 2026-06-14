#!/usr/bin/env -S uv run
"""
Update the bundled JSON test data for the Reddit app's debug mode.

Usage:
    python3 updateTestData.py              # fetch everything from Reddit
    python3 updateTestData.py --local      # use previously downloaded .json files

By default, the script fetches the JSON feeds and thumbnail images directly
from Reddit using a browser-style User-Agent header. Reddit blocks bare
curl/wget (403), but accepts requests with a full Chrome User-Agent string.

With --local, it skips the JSON fetch and reads aww.hot.json and
subreddit.popular.json from the project root (useful if you already
downloaded them in a browser, or if Reddit changes their blocking).

What the script does:
  1. Fetches (or reads) the aww/hot and subreddits/popular JSON feeds
  2. Validates both have the expected Reddit Listing structure
  3. Downloads each post's thumbnail from Reddit's CDN into assets/thumbs/
  4. Rewrites thumbnail and url fields to point to the local asset copies
  5. Copies the subreddits JSON directly (no image transform needed)
  6. Prints a summary of what it did
"""

import html
import json
import os
import shutil
import subprocess
import sys

# ---------------------------------------------------------------------------
# Path constants
# ---------------------------------------------------------------------------

# Directory this script lives in (project root for the Reddit app)
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# Android assets directory where the app reads bundled test data
ASSETS_DIR = os.path.join(SCRIPT_DIR, "app", "src", "main", "assets")

# Subdirectory inside assets/ for downloaded thumbnail images
THUMBS_DIR = os.path.join(ASSETS_DIR, "thumbs")

# --- Local JSON files (in project root) ---
# These are written by the fetch step and read by the transform step.
# With --local mode, the fetch step is skipped and these must already exist.
AWW_JSON = os.path.join(SCRIPT_DIR, "aww.hot.json")
SUBS_JSON = os.path.join(SCRIPT_DIR, "subreddit.popular.json")

# --- Output files (in assets/) ---
# Transformed posts JSON with local asset URLs replacing Reddit image URLs
AWW_TRANSFORMED_OUTPUT = os.path.join(ASSETS_DIR, "aww.hot.1.100.json.transformed.txt")
# Copy of the subreddits JSON
SUBS_OUTPUT = os.path.join(ASSETS_DIR, "subreddits.1.json.txt")

# --- Reddit API URLs ---
AWW_URL = "https://www.reddit.com/r/aww/hot.json?limit=100"
SUBS_URL = "https://www.reddit.com/subreddits/popular.json?limit=100"

# --- Fallback images for posts without a downloadable thumbnail ---
# These are the original bundled images, used when a post's thumbnail
# field is "self", "default", "nsfw", "spoiler", or a download fails.
FALLBACK_IMGS = [
    "file:///android_asset/bigcat0.jpg",
    "file:///android_asset/bigcat1.jpg",
    "file:///android_asset/bigcat2.jpg",
    "file:///android_asset/bigdog0.jpg",
]

# Browser-style User-Agent header. Reddit returns 403 for bare curl/wget
# but accepts requests that look like they come from a real browser.
# Used for both JSON fetches and thumbnail downloads.
CURL_USER_AGENT = (
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
)

# Timeout in seconds for each thumbnail download
DOWNLOAD_TIMEOUT = 15


# ---------------------------------------------------------------------------
# JSON fetching via curl
# ---------------------------------------------------------------------------

def fetch_json_with_curl(url, dest_path):
    """Fetch a JSON URL using curl with a browser User-Agent.

    Reddit blocks requests without a realistic User-Agent header,
    returning 403. Using a full Chrome UA string avoids this.

    Args:
        url: The Reddit JSON API URL to fetch
        dest_path: Local file path to save the response body

    Raises:
        SystemExit: If curl fails or returns a non-200 status
    """
    print(f"  Fetching {url} ...")
    result = subprocess.run(
        [
            "curl",
            "--silent",             # No progress bar
            "--fail",               # Exit with error on HTTP errors (4xx, 5xx)
            "--location",           # Follow redirects
            "--output", dest_path,  # Write response body to file
            "--header", f"User-Agent: {CURL_USER_AGENT}",
            url,
        ],
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        print(f"ERROR: curl failed (exit code {result.returncode})")
        if result.stderr:
            print(f"  stderr: {result.stderr.strip()}")
        print("  Try running with --local if you have the JSON files already.")
        sys.exit(1)

    # Sanity check: make sure the file is valid JSON
    try:
        with open(dest_path) as f:
            json.load(f)
    except json.JSONDecodeError as e:
        print(f"ERROR: Response from {url} is not valid JSON: {e}")
        print("  Reddit may have returned an HTML error page.")
        sys.exit(1)

    print(f"  Saved to {os.path.basename(dest_path)}")


# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------

def validate_aww(data):
    """Validate aww.hot.json has the expected Reddit Listing structure.

    Checks:
      - Top-level 'kind' is 'Listing'
      - Has at least one child
      - Each child is kind 't3' (a post) with required fields

    Returns True if valid, False otherwise. Prints stats either way.
    """
    if data.get("kind") != "Listing":
        print("ERROR: aww.hot.json top-level 'kind' is not 'Listing'")
        return False

    children = data.get("data", {}).get("children", [])
    if not children:
        print("ERROR: aww.hot.json has no children")
        return False

    # Fields the app's RedditPost data class needs via @SerializedName
    required_fields = {"name", "title", "score", "num_comments", "thumbnail", "url"}
    for i, child in enumerate(children):
        if child.get("kind") != "t3":
            print(f"WARNING: child {i} kind is '{child.get('kind')}', expected 't3'")
        post = child.get("data", {})
        missing = required_fields - set(post.keys())
        if missing:
            print(f"WARNING: child {i} missing fields: {missing}")

    # Count posts with selftext (useful for knowing which tests are feasible)
    selftext_count = sum(
        1 for c in children if c["data"].get("selftext", "").strip()
    )
    print(f"  Posts: {len(children)}")
    print(f"  Posts with non-empty selftext: {selftext_count}")
    return True


def validate_subs(data):
    """Validate subreddit.popular.json has the expected Reddit Listing structure.

    Checks:
      - Top-level 'kind' is 'Listing'
      - Has at least one child
      - Each child is kind 't5' (a subreddit) with required fields

    Returns True if valid, False otherwise. Prints stats either way.
    """
    if data.get("kind") != "Listing":
        print("ERROR: subreddit.popular.json top-level 'kind' is not 'Listing'")
        return False

    children = data.get("data", {}).get("children", [])
    if not children:
        print("ERROR: subreddit.popular.json has no children")
        return False

    # Fields the app needs for subreddit display
    required_fields = {"name", "display_name"}
    for i, child in enumerate(children):
        if child.get("kind") != "t5":
            print(f"WARNING: subreddit child {i} kind is '{child.get('kind')}', expected 't5'")
        sub = child.get("data", {})
        missing = required_fields - set(sub.keys())
        if missing:
            print(f"WARNING: subreddit child {i} missing fields: {missing}")

    print(f"  Subreddits: {len(children)}")
    return True


# ---------------------------------------------------------------------------
# Thumbnail downloading
# ---------------------------------------------------------------------------

def is_downloadable_url(thumbnail):
    """Check if a thumbnail field contains an actual image URL.

    Reddit uses special string values like "self", "default", "nsfw",
    "spoiler", or "" for posts without a real thumbnail image.
    Only http/https URLs point to actual images on Reddit's CDN.
    """
    return thumbnail.startswith("http://") or thumbnail.startswith("https://")


def download_thumbnail(url, dest_path):
    """Download a single thumbnail image from Reddit's CDN using curl.

    Args:
        url: The thumbnail URL (may contain HTML entities like &amp;)
        dest_path: Local file path to save the image to

    Returns:
        True if download succeeded, False otherwise.
    """
    # Reddit JSON often contains HTML-escaped ampersands in query strings
    clean_url = html.unescape(url)
    result = subprocess.run(
        [
            "curl",
            "--silent",
            "--fail",
            "--location",
            "--max-time", str(DOWNLOAD_TIMEOUT),
            "--output", dest_path,
            "--header", f"User-Agent: {CURL_USER_AGENT}",
            clean_url,
        ],
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        print(f"    WARN: failed to download {clean_url[:60]}...")
        return False
    return True


def download_all_thumbnails(children):
    """Download thumbnail images for all posts into THUMBS_DIR.

    Creates the thumbs/ directory if needed, clears any old thumbnails,
    then downloads each post's thumbnail. Posts without a real thumbnail
    URL get a fallback image instead.

    Args:
        children: The 'children' array from the Reddit Listing JSON

    Returns:
        A list of local asset URL strings, one per child, e.g.:
          "file:///android_asset/thumbs/thumb_0.jpg"
        or a fallback like:
          "file:///android_asset/bigcat0.jpg"
    """
    # Create thumbs/ directory, clearing any stale images from prior runs
    if os.path.exists(THUMBS_DIR):
        shutil.rmtree(THUMBS_DIR)
    os.makedirs(THUMBS_DIR)

    # Track results: one local asset URL per post
    local_urls = []
    downloaded = 0
    fallback_count = 0
    fallback_index = 0  # Cycles through FALLBACK_IMGS for variety

    for i, child in enumerate(children):
        thumbnail = child["data"].get("thumbnail", "")

        if is_downloadable_url(thumbnail):
            # Determine file extension from URL, defaulting to .jpg
            # Thumbnail URLs look like: https://preview.redd.it/abc.jpeg?width=140...
            ext = ".jpg"
            path_part = thumbnail.split("?")[0]  # Strip query params
            if path_part.endswith(".png"):
                ext = ".png"
            elif path_part.endswith(".gif"):
                ext = ".gif"

            # Save as thumb_0.jpg, thumb_1.png, etc.
            filename = f"thumb_{i}{ext}"
            dest = os.path.join(THUMBS_DIR, filename)

            if download_thumbnail(thumbnail, dest):
                local_urls.append(f"file:///android_asset/thumbs/{filename}")
                downloaded += 1
                continue

        # Fallback: no URL, or download failed — use a bundled image
        local_urls.append(FALLBACK_IMGS[fallback_index % len(FALLBACK_IMGS)])
        fallback_index += 1
        fallback_count += 1

    print(f"  Downloaded {downloaded} thumbnails, {fallback_count} using fallback images")
    return local_urls


# ---------------------------------------------------------------------------
# JSON transformation
# ---------------------------------------------------------------------------

def transform_aww(data, local_urls):
    """Replace thumbnail and url fields with local asset paths.

    Both 'thumbnail' and 'url' are set to the same local path because:
      - In debug mode the app loads images via assetFetch(), which uses
        the url field (mapped to imageURL in RedditPost)
      - The thumbnail field is also read by tests to verify image tags
      - Using the same path for both keeps things consistent

    Args:
        data: The parsed aww.hot.json (will be modified in place)
        local_urls: List of local asset URL strings from download_all_thumbnails()

    Returns:
        The modified data dict.
    """
    children = data["data"]["children"]
    for i, child in enumerate(children):
        child["data"]["thumbnail"] = local_urls[i]
        child["data"]["url"] = local_urls[i]
    return data


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    """Entry point: fetch JSON, validate, download thumbnails, write asset files."""

    # Parse command-line flag
    use_local = "--local" in sys.argv

    if use_local:
        # --local mode: skip fetching, require pre-downloaded files
        print("Using local JSON files (--local mode)...")
        missing = []
        if not os.path.exists(AWW_JSON):
            missing.append("aww.hot.json")
        if not os.path.exists(SUBS_JSON):
            missing.append("subreddit.popular.json")
        if missing:
            print(f"ERROR: Missing input files: {', '.join(missing)}")
            print("Download them in a browser, or run without --local to fetch automatically.")
            sys.exit(1)
    else:
        # Default mode: fetch JSON from Reddit via curl
        print("Fetching JSON from Reddit...")
        fetch_json_with_curl(AWW_URL, AWW_JSON)
        fetch_json_with_curl(SUBS_URL, SUBS_JSON)

    # --- Process aww.hot.json ---
    print("Processing aww.hot.json...")
    with open(AWW_JSON) as f:
        aww_data = json.load(f)

    if not validate_aww(aww_data):
        sys.exit(1)

    # Download actual thumbnails from Reddit's CDN
    children = aww_data["data"]["children"]
    print("  Downloading thumbnails...")
    local_urls = download_all_thumbnails(children)

    # Transform: replace image URLs with local asset paths, then write
    transform_aww(aww_data, local_urls)
    with open(AWW_TRANSFORMED_OUTPUT, "w") as f:
        json.dump(aww_data, f)
    print(f"  Wrote: {os.path.relpath(AWW_TRANSFORMED_OUTPUT, SCRIPT_DIR)}")

    # --- Process subreddit.popular.json ---
    print("Processing subreddit.popular.json...")
    with open(SUBS_JSON) as f:
        subs_data = json.load(f)

    if not validate_subs(subs_data):
        sys.exit(1)

    # Subreddits JSON needs no transformation — copy directly
    shutil.copy2(SUBS_JSON, SUBS_OUTPUT)
    print(f"  Wrote: {os.path.relpath(SUBS_OUTPUT, SCRIPT_DIR)}")

    print("Done.")


if __name__ == "__main__":
    main()
