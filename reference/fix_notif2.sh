#!/bin/bash
sed -i 's/}.distinctBy { it.first }/}.distinctBy { it.first }.sortedBy { it.second }/' app/src/main/java/com/example/NotificationHistoryActivity.kt
