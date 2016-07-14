/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.blockshub;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.ArrayList;
import java.util.List;
import static org.primesoft.asyncworldedit.AsyncWorldEditBukkit.log;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;

/**
 *
 * @author SBPrime
 */
public class BlocksHubBridge implements IBlocksHubIntegration {

    /**
     * The current integrator
     */
    private IBlocksHubIntegration m_integrator = new NullBlocksHubIntegration();

    /**
     * List of all factories
     */
    private final IBlocksHubFactory[] m_factories = new IBlocksHubFactory[]{
        new BlocksHubV1Factory(),
        new BlocksHubV2Factory()
    };

    @Override
    public void logBlock(IPlayerEntry playerEntry, IWorld world, Vector location, BaseBlock oldBlock, BaseBlock newBlock) {
        if (!ConfigProvider.getLogBlocks()) {
            return;
        }

        if (playerEntry == null || !playerEntry.isPlayer()
                || playerEntry.isDisposed()
                || playerEntry.getUUID() == null || playerEntry.getName() == null
                || playerEntry.getName().isEmpty()) {
            return;
        }

        m_integrator.logBlock(playerEntry, world, location, oldBlock, newBlock);
    }

    @Override
    public boolean hasAccess(IPlayerEntry playerEntry, IWorld world, Vector location) {
        if (!ConfigProvider.getCheckAccess()) {
            return true;
        }

        if (playerEntry == null || !playerEntry.isPlayer()
                || playerEntry.isDisposed()
                || playerEntry.getUUID() == null || playerEntry.getName() == null
                || playerEntry.getName().isEmpty()) {
            return true;
        }

        return m_integrator.hasAccess(playerEntry, world, location);
    }

    @Override
    public boolean canPlace(IPlayerEntry playerEntry, IWorld world, Vector location, BaseBlock oldBlock, BaseBlock newBlock) {
        if (!ConfigProvider.getCheckAccess()) {
            return true;
        }

        if (playerEntry == null || !playerEntry.isPlayer()
                || playerEntry.isDisposed()
                || playerEntry.getUUID() == null || playerEntry.getName() == null
                || playerEntry.getName().isEmpty()) {
            return true;
        }

        return m_integrator.canPlace(playerEntry, world, location, oldBlock, newBlock);
    }

    public void initialize(Object blocksHubPlugin) {
        if (blocksHubPlugin == null) {
            return;
        }

        log(String.format("Initializing BlocksHub using %1$s...", blocksHubPlugin.getClass().getName()));

        for (IBlocksHubFactory factory : m_factories) {
            IBlocksHubIntegration integrator = create(factory, blocksHubPlugin);

            if (integrator != null) {
                m_integrator = integrator;
                log(String.format("BlocksHub integrator set to %1$s", factory.getName()));
                return;
            }
        }
    }

    /**
     * Try to create a new new bridge
     *
     * @param factory
     * @param blocksHubPlugin
     * @return
     */
    private IBlocksHubIntegration create(IBlocksHubFactory factory, Object blocksHubPlugin) {
        if (factory == null || blocksHubPlugin == null) {
            return null;
        }

        try {
            log(String.format("Trying to use %1$s factory.", factory.getName()));
            return factory.create(blocksHubPlugin);
        } catch (Error ex) {
            //We can ignore this error.
            return null;
        }
    }
}
