# WiSDoM
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fssemenova%2FWiSDoM.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fssemenova%2FWiSDoM?ref=badge_shield)

## [W]orkload Adv[i]sor [S]ervice [D]em[o] [M]odule

To run, execute `Server.java` with the following environmental variables set:

* `js_client`: the full path to the `js_client` folder in this repo
* `glpsol_path`: the full path to the executable `glpsol` program (on a Mac you can 
install it with `brew install homebrew/science/glpk` or Arch with `pacman -S glpk`)

###To generate bundle.min.js file
1. cd to `js_client` directory
2. run `npm install`
3. run `make`


## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fssemenova%2FWiSDoM.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fssemenova%2FWiSDoM?ref=badge_large)