#import <Foundation/Foundation.h>

#import "sqlite3.h"

@interface Primera : NSObject{
	NSInteger myid;
	NSString * col_primaria;
	NSString * col_unica;
	NSInteger col_entero;
}

	@property (nonatomic) NSInteger myid;
	@property (nonatomic) NSString * col_primaria;
	@property (nonatomic) NSString * col_unica;
	@property (nonatomic) NSInteger col_entero;
@end