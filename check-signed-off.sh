
git log 2b2310fef0c4fc70f9002433a1844e050f926830..HEAD \
  --pretty=format:"%H" | while read c; do
  git show -s --format=%B $c | grep -q "Signed-off-by" || \
  git show -s --format="%h %s"
done
