# Shopper

![icon](https://raw.githubusercontent.com/fmilitao/shopper-android/master/icons/web_hi_res_256.png )

Shopping list app, experimental project to play with Android SDK.

Features:

* shake to undo
* long press to edit list/item
* short press on list to open
* short press on item to mark as done/undone
* swipe left/right to delete list/item
* import list from clipboard, export to clipboard
* state sored in SQLite database

###Tasks

- [ ] export/import all state to CSV file for easy sharing via email (use sharing intent?)
- [ ] prettify lists/app (idea: show background fill as percentage of list completion, also mess with item count)
- [ ] fix "double tap, double launch" bug on all buttons
- [ ] pick sensible company name
- [ ] capture simple video/gif to show functionality
- [ ] build release .apk and add it to github releases page, [see](http://stackoverflow.com/questions/18460774/how-to-set-up-gradle-and-android-studio-to-do-release-build).