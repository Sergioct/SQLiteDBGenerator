#import <Foundation/Foundation.h>

#import "sqlite3.h"

@interface Segunda : NSObject{
	NSInteger myid;
	long col_fecha;
}

	@property (nonatomic) NSInteger myid;
	@property (nonatomic) long col_fecha;
@end