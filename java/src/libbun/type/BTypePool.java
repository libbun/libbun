package libbun.type;

import libbun.parser.classic.BToken;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.util.Nullable;
import libbun.util.Var;
import libbun.util.BArray;
import libbun.util.BunMap;

public class BTypePool {

	private final static BArray<BType> _TypeList = new BArray<BType>(new BType[128]);

	public final static int _NewTypeId(BType T) {
		@Var int TypeId = BTypePool._TypeList.size();
		BTypePool._TypeList.add(T);
		return TypeId;
	}

	public final static BType TypeOf(int TypeId) {
		if(TypeId == 0) {
			return BType.VarType;
		}
		if(TypeId < BTypePool._TypeList.size()) {
			return BTypePool._TypeList.ArrayValues[TypeId];
		}
		return BType.VarType;
	}

	private final static BunMap<BType>     _ClassNameMap = new BunMap<BType>(null);
	private final static BunMap<BType[]>   _UniqueTypeSetMap = new BunMap<BType[]>(null);

	private final static String _MangleType2(BType Type1, BType Type2) {
		return ":" + Type1.TypeId + ":" + Type2.TypeId;
	}

	private final static String _MangleTypes(BArray<BType> TypeList) {
		@Var String s = "";
		@Var int i = 0;
		while(i < TypeList.size()) {
			@Var BType Type = TypeList.ArrayValues[i];
			s = s + ":" + Type.TypeId;
			i = i + 1;
		}
		return s;
	}

	private final static BType[] _UniqueTypes(BArray<BType> TypeList) {
		@Var String MangleName = "[]" + BTypePool._MangleTypes(TypeList);
		@Var BType[] Types = BTypePool._UniqueTypeSetMap.GetOrNull(MangleName);
		if(Types == null) {
			Types = TypeList.CompactArray();
			BTypePool._UniqueTypeSetMap.put(MangleName, Types);
		}
		return Types;
	}

	public final static BType _GetGenericType1(BType BaseType, BType ParamType) {
		@Var String MangleName = BTypePool._MangleType2(BaseType, ParamType);
		@Var BType GenericType = BTypePool._ClassNameMap.GetOrNull(MangleName);
		if(GenericType == null) {
			GenericType = new BGenericType(BType.UniqueTypeFlag, BaseType, ParamType);
			BTypePool._ClassNameMap.put(MangleName, GenericType);
		}
		return GenericType;
	}

	final static void _SetBaseGenericType(BGenericType Type) {
		@Var String MangleName = BTypePool._MangleType2(Type.BaseType, Type.ParamType);
		BTypePool._ClassNameMap.put(MangleName, Type);
	}

	public final static BType _GetGenericType(BType BaseType, BArray<BType> TypeList, boolean IsCreation) {
		assert(BaseType.GetParamSize() > 0);
		if(TypeList.size() == 1 && !BaseType.IsFuncType()) {
			return BTypePool._GetGenericType1(BaseType, TypeList.ArrayValues[0]);
		}
		@Var String MangleName = ":" + BaseType.TypeId + BTypePool._MangleTypes(TypeList);
		@Var BType GenericType = BTypePool._ClassNameMap.GetOrNull(MangleName);
		if((GenericType == null) && IsCreation) {
			if(BaseType.IsFuncType()) {
				GenericType = new BFuncType(BTypePool._UniqueTypes(TypeList));
			}
			else {
				// TODO;
			}
			BTypePool._ClassNameMap.put(MangleName, GenericType);
		}
		return GenericType;
	}

	public final static BFuncType _LookupFuncType2(BArray<BType> TypeList) {
		@Var BType FuncType = BTypePool._GetGenericType(BFuncType._FuncType, TypeList, true);
		if(FuncType instanceof BFuncType) {
			return (BFuncType)FuncType;
		}
		return null;
	}

	public final static BFuncType _LookupFuncType2(BType R) {
		@Var BArray<BType> TypeList = new BArray<BType>(new BType[2]);
		TypeList.add(R);
		return BTypePool._LookupFuncType2(TypeList);
	}

	public final static BFuncType _LookupFuncType2(BType P1, BType R) {
		@Var BArray<BType> TypeList = new BArray<BType>(new BType[2]);
		TypeList.add(P1);
		TypeList.add(R);
		return BTypePool._LookupFuncType2(TypeList);
	}

	public final static BFuncType _LookupFuncType2(BType P1, BType P2, BType R) {
		@Var BArray<BType> TypeList = new BArray<BType>(new BType[3]);
		TypeList.add(P1);
		TypeList.add(P2);
		TypeList.add(R);
		return BTypePool._LookupFuncType2(TypeList);
	}

	public final static BFuncType _LookupFuncType2(BType P1, BType P2, BType P3, BType R) {
		@Var BArray<BType> TypeList = new BArray<BType>(new BType[3]);
		TypeList.add(P1);
		TypeList.add(P2);
		TypeList.add(P3);
		TypeList.add(R);
		return BTypePool._LookupFuncType2(TypeList);
	}

	public static BType _LookupMutableType(LibBunTypeChecker Gamma, BType Type, @Nullable BToken MutableToken) {
		if(Gamma.IsSupportMutable) {
			@Var String MangleName = "M:"+Type.TypeId;
			@Var BType MutableType = BTypePool._ClassNameMap.GetOrNull(MangleName);
			if(MutableType == null) {
				MutableType = new BMutableType(Type);
				BTypePool._ClassNameMap.put(MangleName, MutableType);
			}
			return MutableType;
		}
		return Type;
	}

	public static BType _LookupNullableType(LibBunTypeChecker Gamma, BType Type, @Nullable BToken MaybeToken) {
		if(Gamma.IsSupportNullable) {
			@Var String MangleName = "N:"+Type.TypeId;
			@Var BType NullableType = BTypePool._ClassNameMap.GetOrNull(MangleName);
			if(NullableType == null) {
				NullableType = new BMutableType(Type);
				BTypePool._ClassNameMap.put(MangleName, NullableType);
			}
			return NullableType;
		}
		return Type;
	}

}
