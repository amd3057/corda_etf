# corda_etf
corda application for ETF Creation and redemption


# Instructions for setting up

1. `git clone https://github.com/amd3057/corda_etf`
2. `cd corda_etf`
3. `./gradlew deployNodes` - building may take upto a minute (it's much quicker if you already have the Corda binaries)./r  
4. `cd java-source/build/nodes`
5. `./runnodes`

At this point you will have notary/network map node running as well as three other nodes and their corresponding webservers. There should be 7 console windows in total. One for the networkmap/notary and two for each of the three nodes. The nodes take about 20-30 seconds to finish booting up.
