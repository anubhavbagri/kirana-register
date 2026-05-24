package com.jarapplication.kiranastore.AOP.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ANNOTATION: @Capitalize - Field-Level Marker for Auto-Uppercasing
 *
 * WHAT IT DOES:
 * ├─ Marks specific fields that should be uppercased
 * ├─ Used WITH @CapitalizeMethod on method
 * └─ Together: Aspect finds marked method → finds marked fields → uppercases them
 *
 * HOW TO USE:
 * ├─ Example 1: Capitalize username field
 * │   public class UserRequest {
 * │       @Capitalize  ← Mark this field
 * │       private String username;
 * │       private String email;  ← Not marked, won't be uppercased
 * │   }
 * │
 * │   In service:
 * │   @CapitalizeMethod  ← Signal to intercept
 * │   public void save(UserRequest req) { ... }
 * │
 * │   When save() called:
 * │   ├─ Aspect intercepts (sees @CapitalizeMethod)
 * │   ├─ Scans UserRequest fields
 * │   ├─ Finds @Capitalize on username
 * │   ├─ Sets username = username.toUpperCase()
 * │   └─ Skips email (no @Capitalize)
 * │
 * └─ Example 2: Multiple marked fields
 *    public class ProductRequest {
 *        @Capitalize
 *        private String category;
 *        @Capitalize
 *        private String color;
 *        private double price;  ← Not marked (and not String anyway)
 *    }
 *
 * @Target({ElementType.FIELD, ElementType.PARAMETER}):
 * ├─ Can be placed on fields OR parameters
 * ├─ FIELD example:
 * │   public class User {
 * │       @Capitalize
 * │       private String name;
 * │   }
 * │
 * └─ PARAMETER example:
 *    public void process(@Capitalize String username) { ... }
 *    └─ Less common, but supported
 *
 * WHY BOTH FIELD AND PARAMETER?
 * ├─ Field: Mark DTO fields that should be uppercased
 * ├─ Parameter: Mark method parameters that should be uppercased
 * └─ Flexibility for different annotation patterns
 *
 * STRING TYPE CHECK:
 * ├─ Aspect only uppercases if field is String type
 * ├─ Field type checking in CapitalizeAspect:
 * │   if (field.getType().equals(String.class)) {
 * │       field.set(entity, value.toUpperCase());
 * │   }
 * │
 * ├─ Reason: Integer, Date, Boolean fields can't be uppercased
 * └─ Type check prevents: .toUpperCase() on non-String types
 *
 * NULL SAFETY:
 * ├─ Aspect checks: if (value != null)
 * ├─ Prevents: NullPointerException if field is null
 * └─ Design: Skip null fields (don't crash, just skip)
 *
 * REAL-WORLD EXAMPLE:
 * ├─ Use case: Normalize user inputs
 * ├─ Problem without @Capitalize:
 * │   ├─ User submits: username=\"JohnDoe\"
 * │   ├─ Another: username=\"johndoe\"
 * │   ├─ Database has: Inconsistent casing (JohnDoe vs johndoe)
 * │   └─ Search fails: findByUsername(\"JOHNDOE\") doesn't find \"JohnDoe\"
 * │
 * └─ Solution with @Capitalize:
 *    ├─ Both users → username \"JOHNDOE\" (consistent)
 *    ├─ Database has: All uppercase (consistent)
 *    └─ Search works: findByUsername(\"JOHNDOE\") always finds match
 *
 * COMPARISON: @Capitalize (FIELD) vs @CapitalizeMethod (METHOD)
 * ├─ @Capitalize:
 * │   ├─ Marks WHICH fields to uppercase
 * │   ├─ Placed on field declarations
 * │   └─ Example: private String username;
 * │
 * └─ @CapitalizeMethod:
 *    ├─ Marks WHICH methods trigger the logic
 *    ├─ Placed on method declarations
 *    └─ Example: public void save(UserRequest req)
 *
 * HOW THEY WORK TOGETHER:
 * ├─ Step 1: You call userService.save(userRequest)
 * ├─ Step 2: CapitalizeAspect sees @CapitalizeMethod on save()
 * ├─ Step 3: Aspect scans UserRequest object
 * ├─ Step 4: Aspect finds @Capitalize on username field
 * ├─ Step 5: Aspect uppercases: username=\"JohnDoe\" → \"JOHNDOE\"
 * ├─ Step 6: Aspect calls actual save() with modified data
 * └─ Step 7: Database receives uppercase username
 *
 * BEST PRACTICES:
 * ├─ ✓ Use on username/email fields (normalize for searches)
 * ├─ ✓ Use on category/type fields (standardize options)
 * ├─ ✓ Use with @CapitalizeMethod (pair together)
 * ├─ ✓ Document why: \"Ensures case-insensitive lookups\"
 * ├─ ✗ DON'T place on non-String fields (wasted annotation)
 * ├─ ✗ DON'T use without @CapitalizeMethod on a service method
 * │   └─ Aspect won't intercept → annotation does nothing
 * └─ ✗ DON'T uppercase fields that shouldn't be (names, emails often keep mixed case)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Capitalize {}
