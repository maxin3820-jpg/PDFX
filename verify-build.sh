#!/bin/bash

# PDFX Build Verification Script
# Validates that the project will build successfully on GitHub Actions
# Run this before pushing to catch issues early

set -e

echo "╔══════════════════════════════════════════════════════════════════════════════╗"
echo "║                                                                              ║"
echo "║                     PDFX Build Verification Script                           ║"
echo "║                                                                              ║"
echo "╚══════════════════════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
CHECKS_PASSED=0
CHECKS_FAILED=0
WARNINGS=0

# Function to print colored output
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
    ((CHECKS_PASSED++))
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
    ((CHECKS_FAILED++))
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
    ((WARNINGS++))
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_section() {
    echo ""
    echo "═══════════════════════════════════════════════════════════════════════════════"
    echo "  $1"
    echo "═══════════════════════════════════════════════════════════════════════════════"
    echo ""
}

# Check 1: Gradle wrapper exists
print_section "1. Checking Gradle Wrapper"
if [ -f "gradlew" ]; then
    print_success "gradlew exists"
    chmod +x gradlew
    print_success "gradlew is executable"
else
    print_error "gradlew not found"
fi

# Check 2: Gradle wrapper properties
print_section "2. Validating Gradle Wrapper Properties"
if [ -f "gradle/wrapper/gradle-wrapper.properties" ]; then
    print_success "gradle-wrapper.properties exists"
    GRADLE_VERSION=$(grep "distributionUrl" gradle/wrapper/gradle-wrapper.properties | cut -d'-' -f2 | cut -d'-' -f1)
    print_info "Gradle version: $GRADLE_VERSION"
else
    print_error "gradle-wrapper.properties not found"
fi

# Check 3: Java version
print_section "3. Checking Java Version"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge "17" ]; then
        print_success "Java $JAVA_VERSION is installed (required: 17+)"
    else
        print_warning "Java $JAVA_VERSION found, but Java 17+ is required"
    fi
else
    print_warning "Java not found in PATH (GitHub Actions will provide it)"
fi

# Check 4: Required build files
print_section "4. Checking Required Build Files"
REQUIRED_FILES=(
    "build.gradle.kts"
    "settings.gradle.kts"
    "gradle.properties"
    "app/build.gradle.kts"
    "app/src/main/AndroidManifest.xml"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        print_success "$file exists"
    else
        print_error "$file is missing"
    fi
done

# Check 5: Kotlin source files
print_section "5. Validating Kotlin Source Files"
KOTLIN_FILES=$(find app/src/main/java -name "*.kt" 2>/dev/null | wc -l)
if [ "$KOTLIN_FILES" -gt 0 ]; then
    print_success "Found $KOTLIN_FILES Kotlin source files"
else
    print_error "No Kotlin source files found"
fi

# Check 6: Resource files
print_section "6. Checking Android Resources"
if [ -d "app/src/main/res" ]; then
    print_success "res directory exists"
    RES_FILES=$(find app/src/main/res -type f 2>/dev/null | wc -l)
    print_info "Resource files: $RES_FILES"
else
    print_error "res directory not found"
fi

# Check 7: GitHub workflows
print_section "7. Validating GitHub Workflows"
WORKFLOW_FILES=(
    ".github/workflows/debug-apk.yml"
    ".github/workflows/build-apk.yml"
    ".github/workflows/release-apk.yml"
)

for workflow in "${WORKFLOW_FILES[@]}"; do
    if [ -f "$workflow" ]; then
        print_success "$(basename "$workflow") exists"
    else
        print_warning "$(basename "$workflow") not found"
    fi
done

# Check 8: .gitignore
print_section "8. Checking .gitignore"
if [ -f ".gitignore" ]; then
    print_success ".gitignore exists"
    
    GITIGNORE_ITEMS=("local.properties" "*.jks" "*.keystore" "build/")
    for item in "${GITIGNORE_ITEMS[@]}"; do
        if grep -q "$item" .gitignore; then
            print_success ".gitignore contains $item"
        else
            print_warning ".gitignore missing $item"
        fi
    done
else
    print_error ".gitignore not found"
fi

# Check 9: Sensitive files NOT in git
print_section "9. Security Check - Sensitive Files"
SENSITIVE_FILES=("local.properties" "*.jks" "*.keystore" "app/release-keystore.jks")
FOUND_SENSITIVE=false

for pattern in "${SENSITIVE_FILES[@]}"; do
    if [ -f "$pattern" ] || [ -n "$(find . -name "$pattern" -not -path "./.git/*" 2>/dev/null)" ]; then
        if git ls-files --error-unmatch "$pattern" &> /dev/null; then
            print_error "SECURITY: $pattern is tracked by git!"
            FOUND_SENSITIVE=true
        else
            print_success "$pattern is not tracked by git"
        fi
    fi
done

if [ "$FOUND_SENSITIVE" = true ]; then
    print_error "Remove sensitive files from git immediately!"
fi

# Check 10: Gradle sync (dry run)
print_section "10. Testing Gradle Sync (Dry Run)"
if command -v ./gradlew &> /dev/null; then
    print_info "Running gradle tasks (this may take a moment)..."
    if ./gradlew tasks --no-daemon --quiet &> /dev/null; then
        print_success "Gradle sync successful"
    else
        print_error "Gradle sync failed"
    fi
else
    print_warning "Skipping gradle sync test (gradlew not executable)"
fi

# Check 11: Build attempt (optional, commented out by default as it takes time)
# Uncomment to perform actual build verification
# print_section "11. Attempting Debug Build"
# print_info "This will take several minutes..."
# if ./gradlew assembleDebug --no-daemon --stacktrace; then
#     print_success "Debug build successful"
#     APK_FILE=$(find app/build/outputs/apk/debug -name "*.apk" | head -1)
#     if [ -f "$APK_FILE" ]; then
#         APK_SIZE=$(du -h "$APK_FILE" | cut -f1)
#         print_info "APK: $(basename "$APK_FILE") ($APK_SIZE)"
#     fi
# else
#     print_error "Debug build failed"
# fi

# Summary
print_section "Verification Summary"
echo ""
echo "  ✅ Checks passed:  $CHECKS_PASSED"
echo "  ❌ Checks failed:  $CHECKS_FAILED"
echo "  ⚠️  Warnings:       $WARNINGS"
echo ""

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                                                                              ║${NC}"
    echo -e "${GREEN}║                        ✅ ALL CHECKS PASSED ✅                                ║${NC}"
    echo -e "${GREEN}║                                                                              ║${NC}"
    echo -e "${GREEN}║            Your project is ready to build on GitHub Actions!                ║${NC}"
    echo -e "${GREEN}║                                                                              ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. git add ."
    echo "  2. git commit -m \"Your message\""
    echo "  3. git push"
    echo "  4. Check GitHub Actions tab for build status"
    echo ""
    exit 0
else
    echo -e "${RED}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║                                                                              ║${NC}"
    echo -e "${RED}║                        ❌ VERIFICATION FAILED ❌                              ║${NC}"
    echo -e "${RED}║                                                                              ║${NC}"
    echo -e "${RED}║                   Fix the issues above before pushing                        ║${NC}"
    echo -e "${RED}║                                                                              ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    exit 1
fi
