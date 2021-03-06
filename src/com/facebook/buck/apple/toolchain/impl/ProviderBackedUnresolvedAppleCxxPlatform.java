/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.apple.toolchain.impl;

import com.facebook.buck.apple.AppleToolchainBuildRule;
import com.facebook.buck.apple.toolchain.AppleCxxPlatform;
import com.facebook.buck.apple.toolchain.UnresolvedAppleCxxPlatform;
import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.Flavor;
import com.facebook.buck.core.model.TargetConfiguration;
import com.facebook.buck.core.rules.BuildRule;
import com.facebook.buck.core.rules.BuildRuleResolver;
import com.facebook.buck.cxx.toolchain.CxxPlatform;
import com.facebook.buck.cxx.toolchain.UnresolvedCxxPlatform;
import com.facebook.buck.swift.toolchain.SwiftPlatform;
import com.facebook.buck.swift.toolchain.UnresolvedSwiftPlatform;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import java.util.Optional;

/**
 * Used to provide a {@link AppleCxxPlatform} that is specified as a apple_toolchain build target.
 */
public class ProviderBackedUnresolvedAppleCxxPlatform implements UnresolvedAppleCxxPlatform {
  private final BuildTarget toolchainTarget;
  private final Flavor flavor;
  private final UnresolvedCxxPlatform cxxPlatformProvider;
  private final UnresolvedSwiftPlatform swiftPlatformProvider;

  public ProviderBackedUnresolvedAppleCxxPlatform(BuildTarget toolchainTarget, Flavor flavor) {
    this.toolchainTarget = toolchainTarget;
    this.flavor = flavor;
    this.cxxPlatformProvider =
        new UnresolvedCxxPlatform() {
          @Override
          public CxxPlatform resolve(
              BuildRuleResolver resolver, TargetConfiguration targetConfiguration) {
            return ProviderBackedUnresolvedAppleCxxPlatform.this.resolve(resolver).getCxxPlatform();
          }

          @Override
          public Flavor getFlavor() {
            return flavor;
          }

          @Override
          public UnresolvedCxxPlatform withFlavor(Flavor hostFlavor) {
            throw new UnsupportedOperationException();
          }

          @Override
          public Iterable<BuildTarget> getParseTimeDeps(TargetConfiguration targetConfiguration) {
            return ProviderBackedUnresolvedAppleCxxPlatform.this.getParseTimeDeps(
                targetConfiguration);
          }

          @Override
          public Iterable<? extends BuildTarget> getLinkerParseTimeDeps(
              TargetConfiguration targetConfiguration) {
            return getParseTimeDeps(targetConfiguration);
          }
        };
    this.swiftPlatformProvider =
        new UnresolvedSwiftPlatform() {
          @Override
          public Iterable<BuildTarget> getParseTimeDeps(TargetConfiguration targetConfiguration) {
            return ProviderBackedUnresolvedAppleCxxPlatform.this.getParseTimeDeps(
                targetConfiguration);
          }

          @Override
          public Optional<SwiftPlatform> resolve(BuildRuleResolver ruleResolver) {
            return ProviderBackedUnresolvedAppleCxxPlatform.this
                .resolve(ruleResolver)
                .getSwiftPlatform();
          }

          @Override
          public Flavor getFlavor() {
            return flavor;
          }
        };
  }

  @Override
  public Iterable<BuildTarget> getParseTimeDeps(TargetConfiguration targetConfiguration) {
    return ImmutableList.of(toolchainTarget);
  }

  @Override
  public AppleCxxPlatform resolve(BuildRuleResolver ruleResolver) {
    BuildRule rule = ruleResolver.getRule(toolchainTarget);
    Verify.verify(rule instanceof AppleToolchainBuildRule);
    return ((AppleToolchainBuildRule) rule).getAppleCxxPlatform(flavor);
  }

  @Override
  public UnresolvedCxxPlatform getUnresolvedCxxPlatform() {
    return cxxPlatformProvider;
  }

  @Override
  public UnresolvedSwiftPlatform getUnresolvedSwiftPlatform() {
    return swiftPlatformProvider;
  }

  @Override
  public Flavor getFlavor() {
    return flavor;
  }
}
