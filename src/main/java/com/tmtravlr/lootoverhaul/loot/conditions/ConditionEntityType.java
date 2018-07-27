package com.tmtravlr.lootoverhaul.loot.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.tmtravlr.lootoverhaul.LootOverhaul;
import com.tmtravlr.lootoverhaul.loot.LootHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

/**
 * Will pass if the entity killed/looted/etc. has this type or category.
 * 
 * Example Usage:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:entity_type",
 *  		"type": [
 *  			"minecraft:zombie",
 *  			"minecraft:skeleton"
 *  		]
 *  	}
 *  ]
 *  Or with only one name:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:entity_type",
 *  		"type": "minecraft:chest_minecart"
 *  	}
 *  ]
 *  Or with a category:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:entity_type",
 *  		"category": "ANIMAL"
 *  	}
 *  ]
 *  Or with several categories:
 * "conditions": [
 *  	{
 *  		"condition": "lootoverhaul:entity_type",
 *  		"category": [
 *  			"UNDEAD",
 *  			"ILLAGER"
 *  		]
 *  	}
 *  ]
 * 
 * @author Tmtravlr (Rebeca Rey)
 * @since November 2016
 */
public class ConditionEntityType implements LootCondition {
	
	private String[] entityTypes;
	private List<EntityCategory> entityCategories = new ArrayList<>();
	
	public ConditionEntityType(String[] entityTypesToSet, List<EntityCategory> entityCategoriesToSet) {
		this.entityTypes = entityTypesToSet;
		this.entityCategories = entityCategoriesToSet;
    }
	
	@Override
	public boolean testCondition(Random rand, LootContext context) {
		
		Entity entity = null;
		boolean matches = false;
		
		if(context.getLootedEntity() != null) {
			entity = context.getLootedEntity();
		}
		
		if (entity != null) {
			if (entityTypes != null) {
				for (String entityTypeName : entityTypes) {
					ResourceLocation entityType = new ResourceLocation(entityTypeName);
					if (entityType.equals(EntityList.getKey(entity))) {
						matches = true;
					}
				}
			}
			
			for (EntityCategory category : entityCategories) {
				if (category.getCreatureAttribute() != null) {
					if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getCreatureAttribute() == category.getCreatureAttribute()) {
						matches = true;
					}
				}
				
				if (category == EntityCategory.MONSTER) {
					if (entity instanceof IMob) {
						matches = true;
					}
				} else if (category == EntityCategory.ANIMAL) {
					if (entity instanceof EntityAnimal) {
						matches = true;
					}
				} else if (category == EntityCategory.AMBIENT) {
					if (entity instanceof EntityAmbientCreature || entity instanceof EntitySquid) {
						matches = true;
					}
				} else if (category == EntityCategory.NPC) {
					if (entity instanceof INpc) {
						matches = true;
					}
				} else if (category == EntityCategory.GOLEM) {
					if (entity instanceof EntityGolem) {
						matches = true;
					}
				}
			}
		}
		
		return matches;
	}

	public static class Serializer extends LootCondition.Serializer<ConditionEntityType> {
    	
        public Serializer() {
            super(new ResourceLocation(LootOverhaul.MOD_ID, "entity_type"), ConditionEntityType.class);
        }

        public void serialize(JsonObject json, ConditionEntityType value, JsonSerializationContext context) {
        	if (value.entityTypes != null) {
        		LootHelper.serializeStringArray(value.entityTypes, json, "type");
        	}
        	if (!value.entityCategories.isEmpty()) {
        		LootHelper.serializeStringArray(value.entityCategories.stream().map(EntityCategory::name).collect(Collectors.toList()).toArray(new String[0]), json, "category");
        	}
        }

        public ConditionEntityType deserialize(JsonObject json, JsonDeserializationContext context) {
        	if (!(json.has("type") || json.has("category"))) {
        		throw new JsonSyntaxException("Expected one or both fields 'type' and 'category'");
        	}
        	
        	String[] entityTypes = null;
        	String[] entityCategoryNames = null;
        	List<EntityCategory> entityCategories = new ArrayList<>();
        	
        	if (json.has("type")) {
        		entityTypes = LootHelper.deserializeStringArray(json, "type");
        	}
        	
        	if (json.has("category")) {
        		entityCategoryNames = LootHelper.deserializeStringArray(json, "category");
        	}
        	
        	if (entityCategoryNames != null) {
        		Arrays.stream(entityCategoryNames).forEach(categoryName -> {
        			try {
        				entityCategories.add(EntityCategory.valueOf(categoryName.toUpperCase()));
        			} catch (IllegalArgumentException e) {
        				throw new JsonSyntaxException("Invalid entity category name '" + categoryName + "'. Possible values are MONSTER, ANIMAL, AMBIENT, NPC, GOLEM, UNDEAD, ARTHROPOD, ILLAGER", e);
        			}
        		});
        	}
        	
        	return new ConditionEntityType(entityTypes, entityCategories);
        }
    }
	
	private enum EntityCategory {
		MONSTER(),
		ANIMAL(),
		AMBIENT(),
		NPC(),
		GOLEM(),
		UNDEAD(EnumCreatureAttribute.UNDEAD),
		ARTHROPOD(EnumCreatureAttribute.ARTHROPOD),
		ILLAGER(EnumCreatureAttribute.ILLAGER);
		
		private EnumCreatureAttribute creatureAttribute;
		
		private EntityCategory() {
			this(null);
		}
		
		private EntityCategory(EnumCreatureAttribute creatureAttribute) {
			this.creatureAttribute = creatureAttribute;
		}
		
		public EnumCreatureAttribute getCreatureAttribute() {
			return this.creatureAttribute;
		}
	}

}
