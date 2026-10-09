#!/bin/bash
set -e

SOURCE="/Users/wuyunbin/workspace/rocketmq/rocketmq-demo"
TARGET="/Users/wuyunbin/workspace/rocketmq/rocketmq-demo/student-repos"
SRC_SERVICE="src/main/java/com/example/rocketmq/service"
SRC_TEST="src/test/java/com/example/rocketmq"

echo "===== start init student repos ====="
echo "source: $SOURCE"
echo "target: $TARGET"
echo ""

# Clean old student repos
if [ -d "$TARGET" ]; then
    rm -rf "$TARGET"
fi

# Base files for every day
BASE_FILES="pom.xml .gitignore src/main/resources/application.properties src/main/java/com/example/rocketmq/RocketmqCourseApplication.java src/main/java/com/example/rocketmq/config/RocketMQConfig.java"

# Get services for a given day（每日新增的业务 Service，累积拷贝 day1~dayN）
get_services() {
    case $1 in
        1) echo "" ;;
        2) echo "SyncProducerService.java" ;;
        3) echo "QuickConsumerService.java" ;;
        4) echo "SendModeService.java" ;;
        5) echo "TagFilterService.java" ;;
        6) echo "BroadcastService.java" ;;
        7) echo "OrderNotifyService.java" ;;
        8) echo "OrderSequenceService.java" ;;
        9) echo "DelayCloseOrderService.java" ;;
        10) echo "BatchSendService.java PropertyFilterService.java" ;;
        11) echo "RetryAndIdempotentService.java DeadLetterService.java" ;;
        12) echo "TransactionOrderService.java" ;;
        13) echo "BacklogHandlerService.java" ;;
        14) echo "HaDeployService.java" ;;
        15) echo "TradeLinkService.java" ;;
    esac
}

# Get tests for a given day
get_tests() {
    case $1 in
        1) echo "Day01QuickStartTest.java" ;;
        2) echo "Day02SyncSendTest.java" ;;
        3) echo "Day03ConsumeTest.java" ;;
        4) echo "Day04SendModeTest.java" ;;
        5) echo "Day05TagFilterTest.java" ;;
        6) echo "Day06BroadcastTest.java" ;;
        7) echo "Day07OrderNotifyTest.java" ;;
        8) echo "Day08SequenceTest.java" ;;
        9) echo "Day09DelayTest.java" ;;
        10) echo "Day10BatchFilterTest.java" ;;
        11) echo "Day11RetryIdempotentTest.java" ;;
        12) echo "Day12TransactionTest.java" ;;
        13) echo "Day13BacklogTest.java" ;;
        14) echo "Day14HaTest.java" ;;
        15) echo "Day15TradeLinkTest.java" ;;
    esac
}

# 仅当源测试文件存在时才生成该 day 的仓库，避免空仓库误报编译失败
for day in $(seq 1 15); do
    day_padded=$(printf "%02d" $day)
    tests=$(get_tests $day)
    has_src=false
    if [ -n "$tests" ]; then
        for t in $tests; do
            if [ -f "$SOURCE/$SRC_TEST/$t" ]; then
                has_src=true
                break
            fi
        done
    fi
    if [ "$has_src" = false ]; then
        echo "--- Day $day_padded: SKIP（无源测试文件） ---"
        continue
    fi

    repo_dir="$TARGET/day$day_padded"
    echo "--- Day $day_padded: $repo_dir ---"
    mkdir -p "$repo_dir"

    # 1. Copy base files
    echo "  [1/4] copy base files..."
    for f in $BASE_FILES; do
        dest="$repo_dir/$f"
        mkdir -p "$(dirname "$dest")"
        cp "$SOURCE/$f" "$dest"
    done

    # 2. Copy cumulative services (day1..dayN)
    echo "  [2/4] copy services (Day 1 ~ Day $day)..."
    svc_count=0
    for d in $(seq 1 $day); do
        services=$(get_services $d)
        if [ -n "$services" ]; then
            for svc in $services; do
                src="$SOURCE/$SRC_SERVICE/$svc"
                dest="$repo_dir/$SRC_SERVICE/$svc"
                mkdir -p "$(dirname "$dest")"
                if [ -f "$src" ]; then
                    cp "$src" "$dest"
                    svc_count=$((svc_count + 1))
                else
                    echo "    WARNING: missing $src"
                fi
            done
        fi
    done
    echo "    $svc_count service files"

    # 3. Copy cumulative tests (day1..dayN)
    echo "  [3/4] copy tests (Day 1 ~ Day $day)..."
    test_count=0
    for d in $(seq 1 $day); do
        tests=$(get_tests $d)
        if [ -n "$tests" ]; then
            for t in $tests; do
                src="$SOURCE/$SRC_TEST/$t"
                dest="$repo_dir/$SRC_TEST/$t"
                mkdir -p "$(dirname "$dest")"
                if [ -f "$src" ]; then
                    cp "$src" "$dest"
                    test_count=$((test_count + 1))
                else
                    echo "    WARNING: missing $src"
                fi
            done
        fi
    done
    echo "    $test_count test files"

    # 4. Git init
    echo "  [4/4] git init..."
    cd "$repo_dir"
    git init -q 2>/dev/null || git init
    git add -A
    git commit -q -m "Day $day_padded: RocketMQ course day $day"
    cd "$SOURCE"

    echo "  DONE Day $day_padded"
    echo ""
done

echo "===== verify compile ====="
FAILED=""
for day in $(seq 1 15); do
    day_padded=$(printf "%02d" $day)
    repo_dir="$TARGET/day$day_padded"
    if [ ! -d "$repo_dir" ]; then
        continue
    fi
    echo -n "Day $day_padded: "
    cd "$repo_dir"
    if mvn test-compile -q 2>/dev/null; then
        echo "PASS"
    else
        echo "FAIL"
        FAILED="$FAILED day$day_padded"
    fi
    cd "$SOURCE"
done

echo ""
if [ -n "$FAILED" ]; then
    echo "FAILED: $FAILED"
else
    echo "All generated repos compiled successfully!"
fi
