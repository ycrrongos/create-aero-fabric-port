# Network proxy for this port

**Shell / git / curl (GitHub):** FlClash SOCKS5 `127.0.0.1:7890`  
`export ALL_PROXY=socks5://127.0.0.1:7890`

**Gradle / Java Maven:** do **not** set `systemProp.socksProxyHost`.  
Java HTTPS through that SOCKS breaks TLS (neoform etc.). Maven hosts work direct.

GitHub raw Maven repos are `exclusiveContent`-scoped so Gradle won't probe them for NeoForm.
