#!/bin/bash
# Generate API Docs file
redoc-cli bundle ./api-reference-live.yaml --options.theme.spacing.sectionVertical=1 --options.theme.typography.fontSize=‘16px’ --options.hideDownloadButton=true

# Remove "Documentation Powered by ReDoc" Link at the bottom left
sed 's/Documentation Powered by ReDoc//g' redoc-static.html > temp.html

# Update Title and Favicon
sed 's_<title>ReDoc documentation</title>_<title>Fizz documentation</title><link rel="icon" type="image/png" href="images/favicon-32x32.png" sizes="32x32" /><link rel="icon" type="image/png" href="images/favicon-16x16.png" sizes="16x16" />_g' temp.html > index.html

# Remove temporary files
rm temp.html
rm redoc-static.html