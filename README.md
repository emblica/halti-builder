# halti-builder

Docker image builder for Halti/Funnel


## Build

```

docker build -t emblica/halti-builder .
```


## Usage

Not yet for production / Work in process

```
PORT=4041
PRODUCTION=no
```

```
docker run -d -p 10.4.1.224:4041:4041 --name halti-builder --restart=always -e PORT=4041 -e PRODUCTION=yes emblica/halti-builder
```


### Bugs

...

## License
`See LICENCE`
Copyright © 2016 Emblica
