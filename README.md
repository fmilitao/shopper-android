# Shopper

![icon](https://raw.githubusercontent.com/fmilitao/shopper-android/master/icons/web_hi_res_256.png )

Shopping list app, experimental project to play with Android SDK.

Features:

* shake to undo deletion
* long press to edit list/item
* short press on list to open
* short press on item to mark as done/undone
* swipe left/right to delete list/item
* transfer items between lists
* import list from clipboard, export to clipboard
* state stored in SQLite database
* animations for sorting done/not done items & transition between activities
* export/import individual list to/from CSV file
* item categories: add new, change background color or show category text

###Tasks

- [ ] weird bug with some TextViews underlined red/blue ?!
- [ ] allow labels?
- [ ] add interface for importing from clipboard, try different patterns
- [ ] add categories to copy-paste format (but what format to use??)
- [ ] prettify lists/app (idea: show background fill as percentage of list completion, also mess with item count)
- [ ] capture simple video/gif to show functionality
- [ ] build release .apk and add it to github releases page, [see](http://stackoverflow.com/questions/18460774/how-to-set-up-gradle-and-android-studio-to-do-release-build).