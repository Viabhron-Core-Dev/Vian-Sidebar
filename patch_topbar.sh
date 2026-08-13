#!/bin/bash
cat app/src/main/java/com/example/feature/sidebar/SidebarView.kt | awk '
BEGIN { skip = 0; }
/val marginDp = 8f/ { skip = 1; }
/viewPager = ViewPager2/ { skip = 0; }
{
    if (!skip) print $0
    if (skip == 1) {
        skip = 2;
        print "        // --- BEGIN REPLACEMENT ---"
        print "        // Padding removed for edge-to-edge layout"
        print ""
        print "        val isLooping = pageConfigs.size > 2"
        print "        val startingIndex = if (isLooping) {"
        print "            val half = Int.MAX_VALUE / 2"
        print "            half - (half % pageConfigs.size) + max(0, defaultPageIndex)"
        print "        } else {"
        print "            max(0, defaultPageIndex)"
        print "        }"
        print ""
        print "        // ViewPager must be added FIRST so the header floats on top"
        print "        viewPager = ViewPager2(context).apply {"
        print "            layoutParams = if (wrapContent) {"
        print "                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)"
        print "            } else {"
        print "                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)"
        print "            }"
        print "            offscreenPageLimit = if (containerId == \"sidebar\") 1 else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT"
        print "        }"
        print "        // Adapter setup will happen after viewPager initialization"
    }
}
' > temp.kt
mv temp.kt app/src/main/java/com/example/feature/sidebar/SidebarView.kt
