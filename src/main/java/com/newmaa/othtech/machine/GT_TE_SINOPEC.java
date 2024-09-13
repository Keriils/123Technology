package com.newmaa.othtech.machine;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.withChannel;
import static gregtech.api.GregTech_API.sBlockCasings2;
import static gregtech.api.GregTech_API.sBlockCasings3;
import static gregtech.api.GregTech_API.sBlockCasings4;
import static gregtech.api.GregTech_API.sBlockCasings8;
import static gregtech.api.GregTech_API.sBlockConcretes;
import static gregtech.api.GregTech_API.sBlockMetal1;
import static gregtech.api.GregTech_API.sBlockMetal6;
import static gregtech.api.GregTech_API.sBlockMetal7;
import static gregtech.api.enums.GT_HatchElement.Energy;
import static gregtech.api.enums.GT_HatchElement.ExoticEnergy;
import static gregtech.api.enums.GT_HatchElement.InputBus;
import static gregtech.api.enums.GT_HatchElement.InputHatch;
import static gregtech.api.enums.GT_HatchElement.Muffler;
import static gregtech.api.enums.GT_HatchElement.OutputBus;
import static gregtech.api.enums.GT_HatchElement.OutputHatch;
import static gregtech.api.enums.Mods.Chisel;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_GLOW;
import static gregtech.api.util.GT_StructureUtility.ofCoil;
import static gregtech.api.util.GT_StructureUtility.ofFrame;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.github.bartimaeusnek.bartworks.API.BorosilicateGlass;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.newmaa.othtech.Utils.Utils;
import com.newmaa.othtech.common.recipemap.Recipemaps;
import com.newmaa.othtech.machine.machineclass.OTH_MultiMachineBase;
import com.newmaa.othtech.machine.machineclass.OTH_processingLogics.OTH_ProcessingLogic;

import gregtech.api.GregTech_API;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_HatchElementBuilder;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Utility;

public class GT_TE_SINOPEC extends OTH_MultiMachineBase<GT_TE_SINOPEC> {

    public GT_TE_SINOPEC(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_TE_SINOPEC(String aName) {
        super(aName);
    }

    private byte mode = 0;

    private HeatingCoilLevel coilLevel;

    public HeatingCoilLevel getCoilLevel() {
        return this.coilLevel;
    }

    public void setCoilLevel(HeatingCoilLevel coilLevel) {
        this.coilLevel = coilLevel;
    }

    public int getCoilTier() {
        return Utils.getCoilTier(coilLevel);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
    }

    @Override
    protected boolean isEnablePerfectOverclock() {
        if (getControllerSlot() == GT_ModHandler.getModItem("123Technology", "dustIrOsSm", 1)) {
            return true;
        }
        return false;
    }

    protected int getMaxParallelRecipes() {
        if (getControllerSlot() == GT_ModHandler.getModItem("123Technology", "dustIrOsSm", 1)) {
            return 256;
        }
        return 64;
    }

    protected float getSpeedBonus() {
        if (getCoilTier() >= 10) {
            return 0.01F;
        }
        return 1 - getCoilTier() * 0.1F;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return Recipemaps.SINOPEC;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {

        return new OTH_ProcessingLogic() {

            @NotNull
            @Override
            public CheckRecipeResult process() {
                setSpeedBonus(getSpeedBonus());
                return super.process();
            }

        }.setMaxParallelSupplier(this::getMaxParallelRecipes);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        repairMachine();
        coilLevel = HeatingCoilLevel.None;

        return checkPiece(STRUCTURE_PIECE_MAIN, horizontalOffSet, verticalOffSet, depthOffSet);

    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        repairMachine();
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, horizontalOffSet, verticalOffSet, depthOffSet);

    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (this.mMachine) return -1;
        int realBudget = elementBudget >= 200 ? elementBudget : Math.min(200, elementBudget * 5);

        return this.survivialBuildPiece(
            STRUCTURE_PIECE_MAIN,
            stackSize,
            horizontalOffSet,
            verticalOffSet,
            depthOffSet,
            realBudget,
            env,
            false,
            true);

    }

    private static final String STRUCTURE_PIECE_MAIN = "main";

    private final int horizontalOffSet = 26;
    private final int verticalOffSet = 46;
    private final int depthOffSet = 2;
    private static final int HORIZONTAL_DIRT_METAID = 10;
    private static IStructureDefinition<GT_TE_SINOPEC> STRUCTURE_DEFINITION = null;

    @Override
    public IStructureDefinition<GT_TE_SINOPEC> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<GT_TE_SINOPEC>builder()
                .addShape(STRUCTURE_PIECE_MAIN, shapeMain)
                .addElement('A', BorosilicateGlass.ofBoroGlass(3))
                .addElement('B', BorosilicateGlass.ofBoroGlass(3))
                .addElement('C', ofBlock(sBlockCasings2, 13))
                .addElement('D', ofBlock(sBlockCasings2, 15))
                .addElement('E', ofBlock(sBlockCasings3, 15))
                .addElement('G', ofBlock(sBlockCasings4, 1))
                .addElement('H', withChannel("coil", ofCoil(GT_TE_SINOPEC::setCoilLevel, GT_TE_SINOPEC::getCoilLevel)))
                .addElement('I', ofBlock(sBlockCasings8, 0))
                .addElement('J', ofBlock(sBlockCasings8, 1))
                .addElement('K', ofBlock(sBlockMetal1, 12))
                .addElement('L', ofBlock(sBlockMetal6, 13))
                .addElement('M', ofBlock(sBlockMetal7, 11))
                .addElement('P', ofFrame(Materials.Steel))
                .addElement(
                    'N',
                    (Chisel.isModLoaded() && Block.getBlockFromName(Chisel.ID + ":concrete") != null)
                        ? ofBlock(Block.getBlockFromName(Chisel.ID + ":concrete"), HORIZONTAL_DIRT_METAID)
                        : ofBlock(sBlockConcretes, 0))
                .addElement(
                    'F',
                    GT_HatchElementBuilder.<GT_TE_SINOPEC>builder()
                        .atLeast(Energy.or(ExoticEnergy), InputBus, OutputBus, InputHatch, OutputHatch)
                        .adder(GT_TE_SINOPEC::addToMachineList)
                        .dot(1)
                        .casingIndex(1024 + 12)
                        .buildAndChain(sBlockCasings4, 0))
                .addElement(
                    'O',
                    GT_HatchElementBuilder.<GT_TE_SINOPEC>builder()
                        .atLeast(Muffler)
                        .adder(GT_TE_SINOPEC::addToMachineList)
                        .dot(1)
                        .casingIndex(1024 + 12)
                        .buildAndChain(sBlockMetal7, 11))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    private final String[][] shapeMain = new String[][] {
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                        GGGGG                   ", "                        P   P                   ",
            "                        P   P                   ", "                        P   P                   ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "G   G   G   G   G   G   GGGGG                   ", "P   P   P   P   P   P    FFF                    ",
            "CCCCCCCCCCCCCCCCCCCCCC   F~F                    ", "P   P   P   P   P   PC   FFF                    ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNFFFNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            " DDDDDDDDDDDDDDDDDDDDDDDDGGGG                   ", "                         FDF                    ",
            "CJJJJJJJJJJJJJJJJJJJJJ   FDF                    ", "                     J   FDF                    ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNFFFNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                      GGGGGGGGG ", "                                      P       P ",
            "                                      P       P ", "                                      P       P ",
            " DG G   G   G   G   G   GGGGG         P       P ", "  P P   P   P   P   P    FFF          P       P ",
            "CJCCCCCCCCCCCCCCCCCCCC   FFF          P       P ", "  P P   P   P   P   PC   FFF          P       P ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNFFFNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                      GGGGGGGGG ", "                                                ",
            "                                                ", "                                         GGG    ",
            " D                      GGGGG            GGG    ", "                        P   P            GGG    ",
            "CJC                     P C P             C     ", "                        P C P             C     ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                      GGGGGGGGG ", "                                        P   P   ",
            "                                        PGGGP   ", "                                        GHHHG   ",
            "GDG                                     GHJHG   ", "P P                                     GHHHG   ",
            "CJC                                     PGGGP   ", "P P                                     P   P   ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                      GGGGGGGGG ", "                                                ",
            "                                         GAG    ", "                                        G   G   ",
            " D                                      A J A   ", "                                        G   G   ",
            "CJC                                      GGG    ", "                                                ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                  GGG                           ", "                  GAG                           ",
            "                  GAG                           ", "                  GAG                           ",
            "                  GAG                           ", "                  GAG                           ",
            "                  GAG                           ", "                  GAG                           ",
            "                  GAG                           ", "                  GAG                           ",
            "                  GAG                           ", "                  GAG                           ",
            "                  GAG                           ", "                  GAG                           ",
            "                  GAG                           ", "                  GAG                           ",
            "      GGG   GGG   GAG                           ", "      GAG   GAG   GAG                           ",
            "      GAG   GAG   GAG                           ", "      GAG   GAG   GAG                           ",
            "      GAG   GAG   GAG                           ", "      GAGPPPGAGPPPGAG                           ",
            "      GAG P GAG P GAG                           ", "      GAG P GAG P GAG                           ",
            "      GAGPPPGAGPPPGAG                           ", "      GAG P GAG P GAG                           ",
            "      GAG P GAG P GAG                 GGGGGGGGG ", "      GAGPPPGAGPPPGAG   GGGGGGGGGGGG  P       P ",
            "      GAG P GAG P GAG   P   P  P   P  P  GAG  P ", "      GAG P GAG P GAG   P   P  P   P  P G   G P ",
            " D    GAGPPPGAGPPPGAG   PGGGP  PGGGP  P A J A P ", "      GAG P GAG P GAG   PGAGP  PGAGP  P G   G P ",
            "CJC   GAG P GAG P GAG   PGAGP  PGAGP  P  GGG  P ", "      GGG P GGG P GGG   PGGGP  PGGGP  P       P ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                  GGG                           ",
            "                 G   G                          ", "                 G   G                          ",
            "                 G   G                          ", "                 GHHHG                          ",
            "                 G   G                          ", "                 G   G                          ",
            "                 GHHHG                          ", "                 G   G                          ",
            "                 G   G                          ", "                 GHHHG                          ",
            "                 G   G                          ", "                 G   G                          ",
            "                 GHHHG                          ", "                 G   G                          ",
            "                 G   G                          ", "      GGG   GGG  GHHHG                          ",
            "     G   G G   G G   G                          ", "     G   G G   G G   G                          ",
            "     GHHHG GHHHG GHHHG                          ", "     G   G G   G G   G                          ",
            "     G   G G   G G   G                          ", "     GHHHGPGHHHGPGHHHG                          ",
            "     G   G G   G G   G                          ", "     G   G G   G G   G                          ",
            "     GHHHGPGHHHGPGHHHG                          ", "     G   G G   G G   G                          ",
            "     G   G G   G G   G                GGGGGGGGG ", "     GHHHGPGHHHGPGHHHG  GGGGGGGGGGGG            ",
            "     G   G G   G G   G                   GAG    ", "     G   G G   G G   G   III    III     G   G   ",
            " D   GHHHGPGHHHGPGHHHG  G   G  G   G    A J A   ", "     G   G G   G G   G  G   G  G   G    G   G   ",
            "CJC  G   G G   G G   G  G   G  G   G     GGG    ", "     GHHHG GHHHG GHHHG  GHHHG  GHHHG            ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                  GGG                           ",
            "                 G   G                          ", "                 G   G                          ",
            "                 G   G                          ", "                 GHCHG                          ",
            "                 G   G                          ", "                 G   G                          ",
            "                 GHCHG                          ", "                 G   G                          ",
            "                 G   G                          ", "                 GHCHG                          ",
            "                 G   G                          ", "                 G   G                          ",
            "                 GHCHG                          ", "                 G   G                          ",
            "                 G   G                          ", "      GGG   GGG  GHCHG                          ",
            "     G   G G   G G   G                          ", "     G   G G   G G   G                          ",
            "     GHCHG GHCHG GHCHG                          ", "     G   G G   G G   G                          ",
            "     G   G G   G G   G                          ", "     GHCHGPGHCHGPGHCHG                          ",
            "     G   G G   G G   G                          ", "     G   G G   G G   G                          ",
            "     GHCHGPGHCHGPGHCHG                          ", "     G   G G   G G   G                          ",
            "     G   G G   G G   G                GGGGGGGGG ", "     GHCHGPGHCHGPGHCHG  GGGGGGGGGGGG    P   P   ",
            "     G   G G   G G   G    J      J      PGAGP   ", "     G   G G   G G   G   IJJCCCCIJJCCC  G   G   ",
            "GDG  GHCHGPGHCHGPGHCHG  G J G  G J G C  A J A   ", "P P  G   G G   G G   G  A J A  A J A C  G   G   ",
            "CJC  G   G G   G G   G  A J A  A J A C  PGGGP   ", "P P  GHGHCCCHGHCCCHGHCCCGHGHGIIGHGHG C  P   P   ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNCNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                  GGG                           ",
            "                 G   G                          ", "                 G   G                          ",
            "                 G   G                          ", "                 GHHHG                          ",
            "                 G   G                          ", "                 G   G                          ",
            "                 GHHHG                          ", "                 G   G                          ",
            "                 G   G                          ", "                 GHHHG                          ",
            "                 G   G                          ", "                 G   G                          ",
            "                 GHHHG                          ", "                 G   G                          ",
            "                 G   G                          ", "      GGG   GGG  GHHHG                          ",
            "     G   G G   G G   G                          ", "     G   G G   G G   G                          ",
            "     GHHHG GHHHG GHHHG                          ", "     G   G G   G G   G                          ",
            "     G   G G   G G   G                          ", "     GHHHGPGHHHGPGHHHG                          ",
            "     G   G G   G G   G                          ", "     G   G G   G G   G                          ",
            "     GHHHGPGHHHGPGHHHG                          ", "     G   G G   G G   G                          ",
            "     G   G G   G G   G                GGGGGGGGG ", "     GHHHGPGHHHGPGHHHG  GGGGGGGGGGGG            ",
            "     G   G G   G G   G                   GAG    ", "     G   G G   G G   G   III    III     G   G   ",
            " D   GHHHGPGHHHGPGHHHG  G   G  G   G    A J A   ", "     G   G G   G G   G  G   G  G   G    G   G   ",
            "CJC  G   G G   G G   G  G   G  G   G     GGG    ", "     GHHHG GHHHG GHHHG  GHHHG  GHHHG            ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                  GGG                           ", "                  GGG                           ",
            "                  GGG                           ", "                  GGG                           ",
            "                  GGG                           ", "                  GGG                           ",
            "                  GGG                           ", "                  GGG                           ",
            "                  GGG                           ", "                  GGG                           ",
            "                  GGG                           ", "                  GGG                           ",
            "                  GGG                           ", "                  GGG                           ",
            "                  GGG                           ", "                  GGG                           ",
            "      GGG   GGG   GGG                           ", "      GGG   GGG   GGG                           ",
            "      GGG   GGG   GGG                           ", "      GGG   GGG   GGG                           ",
            "      GGG   GGG   GGG                           ", "      GGGPPPGGGPPPGGG                           ",
            "      GGG   GGG   GGG                           ", "      GGG   GGG   GGG                           ",
            "      GGGPPPGGGPPPGGG                           ", "      GGG   GGG   GGG                           ",
            "      GGG   GGG   GGG                 GGGGGGGGG ", "      GGGPPPGGGPPPGGG   GGGGGGGGGGGG  P       P ",
            "      GGG   GGG   GGG   P   P  P   P  P  GAG  P ", "      GGG   GGG   GGG   P   P  P   P  P G   G P ",
            " D    GGGPPPGGGPPPGGG   PGGGP  PGGGP  P A J A P ", "      GGG P GGG P GGG   PGAGP  PGAGP  P G   G P ",
            "CJC   GGG P GGG P GGG   PGAGP  PGAGP  P  GGG  P ", "      GGG P GGG P GGG   PGGGP  PGGGP  P       P ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                      GGGGGGGGG ", "                                                ",
            "                                         GAG    ", "                                        G   G   ",
            " D                                      A J A   ", "                                        G   G   ",
            "CJC                                      GGG    ", "                                                ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                      GGGGGGGGG ", "                                        P   P   ",
            "                                        PGGGP   ", "                                        GHHHG   ",
            "GDG                                     GHJHG   ", "P P                                     GHHHG   ",
            "CJC                                     PGGGP   ", "P P                                     P   P   ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "        GGGGGGGGG                     GGGGGGGGG ", "        P       P                               ",
            "        P       P                         C     ", "        P       P                        GGG    ",
            " D      P       P    LLL      LLL        GGG    ", "        P       P    LBL      LBL        GGG    ",
            "CJC     P       P    LBL      LBL               ", "        P       P    LLL      LLL               ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "        GGGGGGGGG                     GGGGGGGGG ", "                                      P       P ",
            "                                      P   C   P ", "           GGG       LLL      LLL     P       P ",
            " D         GGG      L   L    L   L    P       P ", "           GGG      L   L    L   L    P       P ",
            "CJC         C       L   L    L   L    P       P ", "            C       LLLLL    LLLLL    P       P ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "        GGGGGGGGG                               ", "          P   P                                 ",
            "          PGCGP                           C     ", "          GHHHG     LLLLL    LLLLL        C     ",
            " D        EH HE    L     L  L     L       C     ", "          GHHHG    L     L  L     L       C     ",
            "CJC       PGGGP    L     L  L     L       C     ", "          P   P    LLLLLLL  LLLLLLL       C     ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNCNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "        GGGGGGGGG                               ", "                                                ",
            "           GCG                                  ", "          G   G     LLLLL    LLLLL              ",
            "GDG       E   E    L     L  L     L             ", "P P       G   G    B     B  B     B             ",
            "CJC        GGG     B     B  B     B             ", "P P                LLLCLLL  LLLCLLL             ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNCNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "        GGGGGGGGG                               ", "        P       P                               ",
            "        P  GCG  P                               ", "        P GHHHG P   LLLLL    LLLLL              ",
            " D      P EH HE P  L     L  L     L             ", "        P GHHHG P  L     L  L     L             ",
            "CJC     P  GGG  P  L     L  L     L             ", "        P       P  LLLLLLL  LLLLLLL             ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNCNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                       LLLLL    ", "                                       LLBLL    ",
            "                                       LLBLL    ", "                                       LLBLL    ",
            "        GGGGGGGGG                      LLBLL    ", "                                       LLBLL    ",
            "           GCG                         LLBLL    ", "          G   G      LLL      LLL      LLBLL    ",
            " D        E   E     L   L    L   L     LLBLL    ", "          G   G     L   L    L   L     LLBLL    ",
            "CJC        GGG      L   L    L   L     LLBLL    ", "                    LLLLL    LLLLL     LLLLL    ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNCNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                       LLLLL    ",
            "                                      L     L   ", "                                      L     L   ",
            "                                      L     L   ", "                                      L     L   ",
            "        GGGGGGGGG                     L     L   ", "          P   P                       L     L   ",
            "          PGCGP                       L     L   ", "          GHHHG       J        J      L     L   ",
            " D        EH HE      LLL      LLL     L     L   ", "          GHHHG      LBL      LBL     L     L   ",
            "CJC       PGGGP      LBL      LBL     L     L   ", "          P   P      LLL      LLL     LLLLLLL   ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                      LLLLLLL   ",
            "                                     L       L  ", "                                     L       L  ",
            "                                     L       L  ", "                                     L       L  ",
            "        GGGGGGGGG                    L       L  ", "                                     L       L  ",
            "           GCG                       L       L  ", "          G   G       J        J     L       L  ",
            "GDG       E   E                      L       L  ", "P P       G   G                      L       L  ",
            "CJC        GGG                       L       L  ", "P P                                  LLLLLLLLL  ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                      LLLLLLL   ",
            "                                     L       L  ", "                                     L       L  ",
            "                                     L       L  ", "                                     L       L  ",
            "        GGGGGGGGG                    L       L  ", "        P       P                    L       L  ",
            "        P  GCG  P                    L       L  ", "        P GHHHG P     J        J     L       L  ",
            " D      P EH HE P     P        P     L       L  ", "        P GHHHJJJJJJJJJJJJJJJJJJJJJJ L       L  ",
            "CJC     P  GGG  P     P        P   J L       L  ", "        P       P     P        P   J LLLLLLLLL  ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNJJJNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                      LLLLLLL   ",
            "                                     L       L  ", "                                     B       B  ",
            "                                     B       B  ", "                                     B       B  ",
            "        GGGGGGGGG                    B       B  ", "                                     B       B  ",
            "           GCG                       B       B  ", "          G   G       J        J     B       B  ",
            " D        E   E                      B       B  ", "          G   G                      B       B  ",
            "CJC        GGG                       B       B  ", "                                     LLLLCLLLL  ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNLNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                      LLLLLLL   ",
            "                                     L       L  ", "                                     L       L  ",
            "                                     L       L  ", "                                     L       L  ",
            "        GGGGGGGGG                    L       L  ", "          P   P                      L       L  ",
            "          PGCGP                      L       L  ", "          GHHHG       J        J     L       L  ",
            " D        EH HE      LLL      LLL    L       L  ", "          GHHHG      LBL      LBL    L       L  ",
            "CJC       PGGGP      LBL      LBL    L       L  ", "          P   P      LLL      LLL    LLLLLLLLL  ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                      LLLLLLL   ",
            "                                     L       L  ", "                                     L       L  ",
            "                                     L       L  ", "                                     L       L  ",
            "        GGGGGGGGG                    L       L  ", "                                     L       L  ",
            "                                     L       L  ", "           GGG       LLL      LLL    L       L  ",
            "GDG        GCG      L   L    L   L   L       L  ", "P P        GGG      L   L    L   L   L       L  ",
            "CJC                 L   L    L   L   L       L  ", "P P                 LLLLL    LLLLL   LLLLLLLLL  ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                       LLLLL    ",
            "                                      L     L   ", "                                      L     L   ",
            "                                      L     L   ", "                                      L     L   ",
            "        GGGGGGGGG                     L     L   ", "        P       P                     L     L   ",
            "        P       P                     L     L   ", "        P       P   LLLLL    LLLLL    L     L   ",
            " D      P   C   P  L     L  L     L   L     L   ", "        P       P  L     L  L     L   L     L   ",
            "CJC     P       P  L     L  L     L   L     L   ", "        P       P  LLLLLLL  LLLLLLL   LLLLLLL   ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "            M                                   ", "            K                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "           PLP                                  ",
            "           PLP                         LLLLL    ", "           PLP                         LLBLL    ",
            "           PLP                         LLBLL    ", "           PLP                         LLBLL    ",
            "        GGGLLLGGG                      LLBLL    ", "           LLL                         LLBLL    ",
            "           LLL                         LLBLL    ", "           LLL      LLLLL    LLLLL     LLBLL    ",
            " D         LCL     L     L  L     L    LLBLL    ", "           LEL     B     B  B     B    LLBLL    ",
            "CJC        LEL     B     B  B     B    LLBLL    ", "           LEL     LLLCLLL  LLLCLLL    LLLLL    ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "           M M                                  ", "           KOK                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "           L L                                  ", "           L L                                  ",
            "        GGGL LGGG                               ", "           L L                                  ",
            "           L L                                  ", "           L L      LLLLL    LLLLL              ",
            " DDDDDDDDDDE E     L     L  L     L             ", " P     P   E E     L     L  L     L             ",
            "CJC    P   E E     L     L  L     L             ", "       P   E E     LLLLLLL  LLLLLLL             ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "            M                                   ", "            K                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "            L                                   ",
            "            L                                   ", "           PLP                                  ",
            "           PLP                                  ", "           PLP                                  ",
            "           PLP                                  ", "           PLP                                  ",
            "        GGGLLLGGG                               ", "           LLL                                  ",
            "           LLL                                  ", "           LLL       LLL      LLL               ",
            "           LEL      L   L    L   L              ", "           LEL      L   L    L   L              ",
            "CJC        LEL      L   L    L   L              ", "CJC        LEL      LLLLL    LLLLL              ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "        GGGGGGGGG                               ", "        P       P                               ",
            "        P       P                               ", "        P       P                               ",
            "        P       P    LLL      LLL               ", "        P       P    LBL      LBL               ",
            "        P       P    LBL      LBL               ", "        P       P    LLL      LLL               ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" },
        { "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "                                                ", "                                                ",
            "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" } };

    @Override
    public boolean addToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        return super.addToMachineList(aTileEntity, aBaseCasingIndex)
            || addExoticEnergyInputToMachineList(aTileEntity, aBaseCasingIndex);
    }

    @Override
    public int getPollutionPerSecond(final ItemStack aStack) {
        return 64000;
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        final GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType("§q§l老登的终极造物 - 中国石化集成工厂")
            .addInfo("§l§a黑金的最终流处...")
            .addInfo("§l一步到位.")
            .addInfo("线圈等级<10时 耗时倍率 = 1 - 线圈等级 * 0.1, ≥10时耗时倍率固定为0.01")
            .addInfo("主机放入铱锇钐合金粉解锁无损超频以及256并行, 并行默认为64")
            .addInfo("§q支持§bTecTech§q能源仓及激光仓，但不支持无线电网直接供给EU")
            .addPollutionAmount(64000)
            .addSeparator()
            .addController("中国石化")
            .beginStructureBlock(49, 76, 49, false)
            .addInputBus("AnyInputBus", 1)
            .addOutputBus("AnyOutputBus", 1)
            .addInputHatch("AnyInputHatch", 1)
            .addOutputHatch("AnyOutputHatch", 1)
            .addEnergyHatch("AnyEnergyHatch", 1)
            .addMufflerHatch("AnyMufflerHatch", 1)
            .toolTipFinisher("§a123Technology - SINOPEC");
        return tt;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    @Override
    public boolean supportsVoidProtection() {
        return true;
    }

    @Override
    public boolean supportsInputSeparation() {
        return true;
    }

    @Override
    public boolean supportsSingleRecipeLocking() {
        return true;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_TE_SINOPEC(this.mName);
    }

    @Override
    public ITexture[] getTexture(final IGregTechTileEntity baseMetaTileEntity, final ForgeDirection sideDirection,
        final ForgeDirection facing, final int aColorIndex, final boolean active, final boolean aRedstone) {

        if (sideDirection == facing) {
            if (active) return new ITexture[] {
                Textures.BlockIcons.getCasingTextureForId(GT_Utility.getCasingTextureIndex(sBlockCasings4, 0)),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE_GLOW)
                    .extFacing()
                    .build() };
            return new ITexture[] {
                Textures.BlockIcons.getCasingTextureForId(GT_Utility.getCasingTextureIndex(sBlockCasings4, 0)),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { Textures.BlockIcons
            .getCasingTextureForId(GT_Utility.getCasingTextureIndex(GregTech_API.sBlockCasings4, 0)) };
    }

    @Override
    protected SoundResource getProcessStartSound() {
        return SoundResource.GT_MACHINES_DISTILLERY_LOOP;
    }
}