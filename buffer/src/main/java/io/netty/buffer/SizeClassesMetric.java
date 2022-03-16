/*
 * Copyright 2020 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer;

/**
 * 暴露SizeClasses的metrics。
 * Expose metrics for an SizeClasses.
 */
public interface SizeClassesMetric {

    /**
     * 根据sizeIdx从lookup table计算size。
     * Computes size from lookup table according to sizeIdx.
     *
     * @return size
     */
    int sizeIdx2size(int sizeIdx);

    /**
     * 根据sizeIdx计算size。
     * Computes size according to sizeIdx.
     *
     * @return size
     */
    int sizeIdx2sizeCompute(int sizeIdx);

    /**
     * 根据pageIdx从lookup table计算size。
     * Computes size from lookup table according to pageIdx.
     *
     * @return size which is multiples of pageSize.
     */
    long pageIdx2size(int pageIdx);

    /**
     * 根据pageIdx计算size。
     * Computes size according to pageIdx.
     *
     * @return size which is multiples of pageSize
     */
    long pageIdx2sizeCompute(int pageIdx);

    /**
     * request size规范化为最接近(向上)的size class。
     * Normalizes request size up to the nearest size class.
     *
     * @param size request size
     *
     * @return sizeIdx of the size class
     */
    int size2SizeIdx(int size);

    /**
     * request size规范化为最接近(向上)的pageSize class。
     * Normalizes request size up to the nearest pageSize class.
     *
     * @param pages multiples of pageSizes
     *
     * @return pageIdx of the pageSize class
     */
    int pages2pageIdx(int pages);

    /**
     * request size规范化为最接近(向下)的pageSize class。
     * Normalizes request size down to the nearest pageSize class.
     *
     * @param pages multiples of pageSizes
     *
     * @return pageIdx of the pageSize class
     */
    int pages2pageIdxFloor(int pages);

    /**
     * 规范化可用的size, 用来分配具有指定size和标准的对象。
     * Normalizes usable size that would result from allocating an object with the
     * specified size and alignment.
     *
     * @param size request size
     *
     * @return normalized size
     */
    int normalizeSize(int size);
}
